(function () {
  var STORAGE_KEY = "warehouse.notifications.recent.v1";
  var MAX_NOTIFICATIONS = 20;
  var activeEventKeysBySource = {};
  var lastRobotStateByKey = {};

  function readNotifications() {
    try {
      var raw = window.localStorage.getItem(STORAGE_KEY);
      var parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed : [];
    } catch (ex) {
      return [];
    }
  }

  function writeNotifications(notifications) {
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(notifications.slice(0, MAX_NOTIFICATIONS)));
    } catch (ex) {
      // The current toast still works even if browser storage is unavailable.
    }
  }

  function formatTimestamp(isoValue) {
    var date = isoValue ? new Date(isoValue) : new Date();

    if (Number.isNaN(date.getTime())) {
      date = new Date();
    }

    return date.toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit"
    });
  }

  function normalizeText(value) {
    return value == null ? "" : String(value).trim();
  }

  function normalizeBatteryPercent(value) {
    var numberValue = Number(value);

    if (!Number.isFinite(numberValue)) {
      return null;
    }

    return Math.max(0, Math.min(100, Math.round(numberValue)));
  }

  function displayMessage(message) {
    if (window.WarehouseSettings && typeof window.WarehouseSettings.translateText === "function") {
      return window.WarehouseSettings.translateText(message);
    }

    return message;
  }

  function createNotification(input) {
    var message = normalizeText(input && input.message);
    var key = normalizeText(input && input.key) || "manual:" + message;

    if (!message) {
      return null;
    }

    return {
      key: key,
      type: normalizeText(input.type) || "info",
      message: message,
      timestamp: input.timestamp || new Date().toISOString()
    };
  }

  function upsertNotification(notification) {
    var notifications = readNotifications()
      .filter(function (item) {
        return item.key !== notification.key;
      });

    notifications.unshift(notification);
    notifications = notifications.slice(0, MAX_NOTIFICATIONS);
    writeNotifications(notifications);
    renderNotificationWidgets(notifications);
  }

  function ensureToastContainer() {
    var container = document.querySelector("[data-notification-toasts]");

    if (container) {
      return container;
    }

    container = document.createElement("div");
    container.className = "notification-toast-container";
    container.dataset.notificationToasts = "true";
    document.body.appendChild(container);
    return container;
  }

  function showToast(notification) {
    if (!document.body) {
      return;
    }

    var container = ensureToastContainer();
    var toast = document.createElement("article");
    var message = document.createElement("div");
    var timestamp = document.createElement("time");

    toast.className = "notification-toast notification-toast-" + notification.type;
    toast.setAttribute("role", "status");
    message.className = "notification-toast-message";
    message.textContent = displayMessage(notification.message);
    timestamp.className = "notification-toast-time";
    timestamp.dateTime = notification.timestamp;
    timestamp.textContent = formatTimestamp(notification.timestamp);

    toast.appendChild(message);
    toast.appendChild(timestamp);
    container.appendChild(toast);

    window.setTimeout(function () {
      toast.classList.add("is-leaving");
      window.setTimeout(function () {
        toast.remove();
      }, 220);
    }, 4200);
  }

  function notify(input, options) {
    var notification = createNotification(input || {});
    var notifyOptions = options || {};

    if (!notification) {
      return;
    }

    upsertNotification(notification);

    if (notifyOptions.toast !== false) {
      showToast(notification);
    }
  }

  function renderNotificationList(listElement, notifications, options) {
    var renderOptions = options || {};

    listElement.innerHTML = "";

    if (!notifications.length) {
      var empty = document.createElement("div");
      empty.className = "notification-empty";
      empty.dataset.i18n = "noNotifications";
      empty.textContent = displayMessage("No notifications yet.");
      listElement.appendChild(empty);
      if (!renderOptions.skipSettingsApply && window.WarehouseSettings) {
        window.WarehouseSettings.apply();
      }
      return;
    }

    notifications.forEach(function (notification) {
      var item = document.createElement("article");
      var message = document.createElement("div");
      var time = document.createElement("time");

      item.className = "notification-item notification-item-" + notification.type;
      message.className = "notification-item-message";
      message.textContent = displayMessage(notification.message);
      time.className = "notification-item-time";
      time.dateTime = notification.timestamp;
      time.textContent = formatTimestamp(notification.timestamp);

      item.appendChild(message);
      item.appendChild(time);
      listElement.appendChild(item);
    });
  }

  function renderNotificationWidgets(notifications, options) {
    var activeNotifications = notifications || readNotifications();
    var renderOptions = options || {};

    document.querySelectorAll("[data-notification-count]").forEach(function (countElement) {
      countElement.textContent = String(activeNotifications.length);
      countElement.hidden = activeNotifications.length === 0;
    });

    document.querySelectorAll("[data-notification-list]").forEach(function (listElement) {
      renderNotificationList(listElement, activeNotifications, renderOptions);
    });
  }

  function clearNotifications() {
    writeNotifications([]);
    renderNotificationWidgets([]);
  }

  function togglePanel(widget) {
    var panel = widget.querySelector("[data-notification-panel]");

    if (!panel) {
      return;
    }

    panel.hidden = !panel.hidden;
  }

  function closeOtherPanels(activeWidget) {
    document.querySelectorAll("[data-notification-widget]").forEach(function (widget) {
      var panel = widget.querySelector("[data-notification-panel]");

      if (panel && widget !== activeWidget) {
        panel.hidden = true;
      }
    });
  }

  function bindNotificationWidgets() {
    document.querySelectorAll("[data-notification-widget]").forEach(function (widget) {
      var toggle = widget.querySelector("[data-notification-toggle]");
      var clearButton = widget.querySelector("[data-notification-clear]");

      if (toggle) {
        toggle.addEventListener("click", function (event) {
          event.stopPropagation();
          closeOtherPanels(widget);
          togglePanel(widget);
        });
      }

      if (clearButton) {
        clearButton.addEventListener("click", function (event) {
          event.stopPropagation();
          clearNotifications();
        });
      }
    });

    document.addEventListener("click", function (event) {
      if (event.target.closest("[data-notification-widget]")) {
        return;
      }
      closeOtherPanels(null);
    });
  }

  function notifyDomEvents() {
    document.querySelectorAll("[data-notification-event]").forEach(function (element) {
      notify({
        key: element.dataset.notificationKey,
        type: element.dataset.notificationType || "info",
        message: element.dataset.notificationMessage
      });
    });
  }

  function robotIdentity(robot) {
    return normalizeText(robot.robotId)
      || normalizeText(robot.id)
      || normalizeText(robot.robotCode)
      || normalizeText(robot.robotName)
      || "robot";
  }

  function missionIdentity(robot) {
    return normalizeText(robot.missionId)
      || normalizeText(robot.requestCode)
      || "no-mission";
  }

  function hasWaitingPathState(robot) {
    var strategyName = normalizeText(robot.currentActiveStrategyName);
    var message = (normalizeText(robot.message) + " " + normalizeText(robot.strategyMessage)).toLowerCase();

    return Boolean(robot.waiting)
      || strategyName === "ObstacleAvoidanceStrategy"
      || message.indexOf("waiting for path") >= 0
      || message.indexOf("waiting for bridge path") >= 0;
  }

  function collectRobotEvents(robot) {
    var robotKey = robotIdentity(robot);
    var missionKey = missionIdentity(robot);
    var events = [];
    var batteryWarningLevel = normalizeText(robot.batteryWarningLevel).toUpperCase();

    if (normalizeText(robot.executionStep) === "RETURNED_TO_BASE" && missionKey !== "no-mission") {
      events.push({
        key: "live-map:" + robotKey + ":" + missionKey + ":returned-to-base",
        type: "info",
        message: "Robot returned to Base. Waiting for confirmation."
      });
    }

    if (batteryWarningLevel === "LOW") {
      events.push({
        key: "live-map:" + robotKey + ":battery-low",
        type: "warning",
        message: "Robot battery is low."
      });
    }

    if (Boolean(robot.chargingRequired)) {
      events.push({
        key: "live-map:" + robotKey + ":" + missionKey + ":charging-required",
        type: "warning",
        message: "Charging required after this task."
      });
    }

    if (Boolean(robot.charging)) {
      events.push({
        key: "live-map:" + robotKey + ":charging-started",
        type: "info",
        message: "Robot is charging at station."
      });
    }

    if (hasWaitingPathState(robot)) {
      events.push({
        key: "live-map:" + robotKey + ":" + missionKey + ":waiting-path",
        type: "warning",
        message: "Robot is waiting for path to clear."
      });
    }

    return events;
  }

  function notifyActiveEventsForSource(sourceKey, events) {
    var previous = activeEventKeysBySource[sourceKey] || {};
    var next = {};

    events.forEach(function (event) {
      next[event.key] = true;

      if (!previous[event.key]) {
        notify(event);
      }
    });

    activeEventKeysBySource[sourceKey] = next;
  }

  function trackChargingTransition(robot) {
    var robotKey = robotIdentity(robot);
    var previous = lastRobotStateByKey[robotKey];
    var batteryPercent = normalizeBatteryPercent(robot.batteryPercent);
    var charging = Boolean(robot.charging);

    if (previous && previous.charging && !charging && batteryPercent === 100) {
      notify({
        key: "live-map:" + robotKey + ":fully-charged",
        type: "success",
        message: "Robot fully charged."
      });
    }

    lastRobotStateByKey[robotKey] = {
      charging: charging,
      batteryPercent: batteryPercent
    };
  }

  function trackLiveMapState(state) {
    if (!state || !Array.isArray(state.robots)) {
      return;
    }

    state.robots.forEach(function (robot) {
      var robotKey = robotIdentity(robot);

      trackChargingTransition(robot);
      notifyActiveEventsForSource("live-map:" + robotKey, collectRobotEvents(robot));
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    bindNotificationWidgets();
    renderNotificationWidgets();
    notifyDomEvents();
  });

  window.WarehouseNotifications = {
    notify: notify,
    clear: clearNotifications,
    render: renderNotificationWidgets,
    trackLiveMapState: trackLiveMapState
  };
})();
