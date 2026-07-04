document.addEventListener("DOMContentLoaded", function () {
  var activeZoneMap = document.getElementById("active-zone-map");
  var stationRow = document.getElementById("station-row");
  var zoneStatus = document.getElementById("zone-status");
  var mapDisplayMode = document.getElementById("map-display-mode");
  var zoneButtons = document.querySelectorAll(".zone-button[data-zone-target]");
  var robotControlButtons = document.querySelectorAll("[data-robot-select]");
  var robotFlowCards = document.querySelectorAll("[data-robot-flow]");
  var missionFlowInstruction = document.getElementById("mission-flow-instruction");
  var routeAnimationMessage = document.getElementById("route-animation-message");
  var showAllRobotsButton = document.getElementById("show-all-robots-button");
  var nextZoneButton = document.getElementById("next-zone-button");
  var startRouteButton = document.getElementById("start-route-button");
  var LIVE_MAP_STATE_URL = "/staff/live-map/state";
  var LIVE_MAP_STATE_POLL_INTERVAL_MS = 1000;
  var ROBOT_VISUAL_NORMAL_MOVEMENT_DURATION_MS = 15000;
  var ROBOT_VISUAL_BRIDGE_MOVEMENT_DURATION_MS = 12000;
  var ROBOT_VISUAL_SEGMENT_SAMPLE_DURATION_MS = 900;
  var ROBOT_VISUAL_MOVEMENT_DURATION_MS = ROBOT_VISUAL_NORMAL_MOVEMENT_DURATION_MS;
  var ROBOT_VISUAL_MOVEMENT_EASING = "linear";
  var OUTBOUND_COLUMN_LEFT = 36.67;
  var RETURN_COLUMN_LEFT = 63.33;
  var demoRoute = ["c", "b", "a"];
  var routeIndex = 0;
  var animationTimer = null;
  var pickupHighlightTimer = null;
  var robotMarkerAnimationFrame = null;
  var liveMapStatePollTimer = null;
  var liveMapStateRequestInFlight = false;
  var robotVisualPositionState = {};
  var routeAnimating = false;
  var routeSegmentDuration = 520;
  var pickupPauseDuration = 850;
  var defaultZone = "c";
  var currentZone = defaultZone;
  var selectedRobotKey = null;
  var svgNamespace = "http://www.w3.org/2000/svg";
  var routeCanvasWidth = 900;
  var routeCanvasHeight = 430;
  var routeWorldWidth = 900;
  var routeWorldHeight = 920;
  var animationRobotKey = null;

  if (!activeZoneMap) {
    return;
  }

  function translateText(value) {
    if (window.WarehouseSettings && typeof window.WarehouseSettings.translateText === "function") {
      return window.WarehouseSettings.translateText(value);
    }

    return value;
  }

  function translateFragment(element) {
    if (element && window.WarehouseSettings && typeof window.WarehouseSettings.translateFragment === "function") {
      window.WarehouseSettings.translateFragment(element);
    }
  }

  function setLocalizedText(element, value) {
    if (!element) {
      return;
    }

    element.dataset.i18nText = value || "";
    element.textContent = value ? translateText(value) : "";
  }

  var routeLinePaths = [
    "M160 95 H330",
    "M330 95 H570",
    "M570 95 H740",
    "M160 215 H330",
    "M330 215 H570",
    "M570 215 H740",
    "M160 335 H330",
    "M330 335 H570",
    "M570 335 H740",
    "M160 95 V335",
    "M330 95 V335",
    "M570 95 V335",
    "M740 95 V335"
  ];

  var routeNodes = [
    { key: "upper-left", x: 160, y: 95 },
    { key: "upper-main", x: 330, y: 95 },
    { key: "upper-cross", x: 570, y: 95 },
    { key: "upper-right", x: 740, y: 95 },
    { key: "middle-left", x: 160, y: 215 },
    { key: "middle-main", x: 330, y: 215 },
    { key: "middle-cross", x: 570, y: 215 },
    { key: "middle-right", x: 740, y: 215 },
    { key: "lower-left", x: 160, y: 335 },
    { key: "lower-main", x: 330, y: 335 },
    { key: "lower-cross", x: 570, y: 335 },
    { key: "lower-right", x: 740, y: 335 }
  ];

  var routeGridSegmentKeys = [
    ["upper-left", "upper-main"],
    ["upper-main", "upper-cross"],
    ["upper-cross", "upper-right"],
    ["middle-left", "middle-main"],
    ["middle-main", "middle-cross"],
    ["middle-cross", "middle-right"],
    ["lower-left", "lower-main"],
    ["lower-main", "lower-cross"],
    ["lower-cross", "lower-right"],
    ["upper-left", "middle-left"],
    ["middle-left", "lower-left"],
    ["upper-main", "middle-main"],
    ["middle-main", "lower-main"],
    ["upper-cross", "middle-cross"],
    ["middle-cross", "lower-cross"],
    ["upper-right", "middle-right"],
    ["middle-right", "lower-right"]
  ];

  var locationCoordinates = {
    a: {
      A1: { left: 27.22, top: 16.74 },
      A2: { left: 50, top: 16.74 },
      A3: { left: 72.78, top: 16.74 },
      A4: { left: 27.22, top: 44.65 },
      A5: { left: 50, top: 44.65 },
      A6: { left: 72.78, top: 44.65 },
      A7: { left: 27.22, top: 72.56 },
      A8: { left: 50, top: 72.56 },
      A9: { left: 72.78, top: 72.56 }
    },
    b: {
      B1: { left: 27.22, top: 16.74 },
      B2: { left: 50, top: 16.74 },
      B3: { left: 72.78, top: 16.74 },
      B4: { left: 27.22, top: 44.65 },
      B5: { left: 50, top: 44.65 },
      B6: { left: 72.78, top: 44.65 },
      B7: { left: 27.22, top: 72.56 },
      B8: { left: 50, top: 72.56 },
      B9: { left: 72.78, top: 72.56 }
    },
    c: {
      C1: { left: 27.22, top: 16.74 },
      C2: { left: 50, top: 16.74 },
      C3: { left: 72.78, top: 16.74 },
      C4: { left: 27.22, top: 44.65 },
      C5: { left: 50, top: 44.65 },
      C6: { left: 72.78, top: 44.65 },
      C7: { left: 27.22, top: 72.56 },
      C8: { left: 50, top: 72.56 },
      C9: { left: 72.78, top: 72.56 }
    }
  };

  var routeWorldZoneLayout = {
    a: { top: 20, left: 0, width: 900, height: 230 },
    b: { top: 330, left: 0, width: 900, height: 230 },
    c: { top: 640, left: 0, width: 900, height: 230 }
  };

  var baseStationPosition = toRouteWorldPosition("c", toRoutePosition(330, 392));
  var chargingStationPosition = toRouteWorldPosition("c", toRoutePosition(570, 392));

  var routeWaypoints = buildRouteWorldWaypoints();

  var outboundRoutePointSequences = {
    c: ["base-station", "zone-c-left-entry"],
    b: ["base-station", "zone-c-left-entry", "zone-c-left-main-1", "zone-c-left-main-2", "bridge-c-b-left-1", "bridge-c-b-left-2", "zone-b-left-entry"],
    a: ["base-station", "zone-c-left-entry", "zone-c-left-main-1", "zone-c-left-main-2", "bridge-c-b-left-1", "bridge-c-b-left-2", "zone-b-left-entry", "zone-b-left-main-1", "zone-b-left-main-2", "bridge-b-a-left-1", "bridge-b-a-left-2", "zone-a-left-entry"]
  };

  var returnRoutePointSequences = {
    c: ["zone-c-right-exit", "base-station"],
    b: ["zone-b-right-exit", "bridge-b-c-right-1", "bridge-b-c-right-2", "zone-c-right-main-2", "zone-c-right-main-1", "zone-c-right-exit", "base-station"],
    a: ["zone-a-right-exit", "bridge-a-b-right-1", "bridge-a-b-right-2", "zone-b-right-main-2", "zone-b-right-main-1", "zone-b-right-exit", "bridge-b-c-right-1", "bridge-b-c-right-2", "zone-c-right-main-2", "zone-c-right-main-1", "zone-c-right-exit", "base-station"]
  };

  var robotDefinitions = {
    picker: {
      label: "Picker Alpha",
      className: "robot-picker",
      currentZone: "a",
      locationCode: "",
      defaultPositions: {
        a: { left: 18, top: 25 },
        b: { left: 22, top: 30 },
        c: { left: 20, top: 34 }
      }
    },
    mover: {
      label: "Mover Beta",
      className: "robot-mover",
      currentZone: "b",
      locationCode: "",
      defaultPositions: {
        a: { left: 81, top: 51 },
        b: { left: 76, top: 46 },
        c: { left: 78, top: 48 }
      }
    },
    carrier: {
      label: "Carrier Gamma",
      className: "robot-carrier",
      currentZone: "c",
      locationCode: "",
      defaultPositions: {
        a: { left: 42, top: 80 },
        b: { left: 47, top: 77 },
        c: { left: 45, top: 74 }
      }
    }
  };

  var zoneData = {
    a: {
      title: "ZONE A",
      subtitle: "SMALL CARGO",
      cargoClass: "cargo-small",
      themeClass: "zone-a",
      showBridge: false,
      showStationConnectors: false,
      label: "Zone A"
    },
    b: {
      title: "ZONE B",
      subtitle: "MEDIUM CARGO",
      cargoClass: "cargo-medium",
      themeClass: "zone-b",
      showBridge: true,
      showStationConnectors: false,
      label: "Zone B"
    },
    c: {
      title: "ZONE C",
      subtitle: "LARGE CARGO",
      cargoClass: "cargo-large",
      themeClass: "zone-c",
      showBridge: true,
      showStationConnectors: true,
      label: "Zone C"
    }
  };

  function normalizeLocationCode(value) {
    return value ? value.trim().toUpperCase() : "";
  }

  function normalizePositionKey(value) {
    var normalizedValue = value ? String(value).trim() : "";

    if (/^[abc][1-9]$/i.test(normalizedValue)) {
      return normalizedValue.toUpperCase();
    }

    if (/^[abc][1-9]-approach$/i.test(normalizedValue)) {
      return normalizedValue.slice(0, 2).toUpperCase() + "-approach";
    }

    return normalizedValue.toLowerCase();
  }

  function hydrateRobotMissionData() {
    robotControlButtons.forEach(function (button) {
      var robotKey = button.dataset.robotSelect;
      var robot = robotDefinitions[robotKey];

      if (!robot) {
        return;
      }

      robot.currentZone = button.dataset.currentZone || button.dataset.zoneTarget || robot.currentZone;
      robot.locationCode = normalizeLocationCode(button.dataset.locationCode || "");
    });
  }

  function getRobotZone(robotKey) {
    var robot = robotDefinitions[robotKey];

    if (robot && robot.positionAnchor && zoneData[robot.positionAnchor.zoneName]) {
      return robot.positionAnchor.zoneName;
    }

    return robot && zoneData[robot.currentZone] ? robot.currentZone : defaultZone;
  }

  function getRobotPosition(robotKey, zoneName) {
    var robot = robotDefinitions[robotKey];
    var locationCode = robot ? robot.locationCode : "";
    var zoneLocations = locationCoordinates[zoneName] || {};

    if (robot && robot.positionAnchor && robot.positionAnchor.zoneName === zoneName) {
      return robot.positionAnchor.position;
    }

    if (locationCode && zoneLocations[locationCode]) {
      return zoneLocations[locationCode];
    }

    return robot.defaultPositions[zoneName] || robot.defaultPositions[defaultZone];
  }

  function hasRobotMissionTarget(robotKey) {
    var robot = robotDefinitions[robotKey];
    var zoneName = getRobotZone(robotKey);
    var zoneLocations = locationCoordinates[zoneName] || {};

    return Boolean(robot && robot.hasActiveMission && robot.locationCode && zoneLocations[robot.locationCode]);
  }

  function isBackendExecutionActive(robot) {
    return Boolean(robot && robot.hasActiveMission && robot.missionStatus === "IN_PROGRESS" &&
      robot.executionStep && robot.executionStep !== "NOT_STARTED");
  }

  function setRouteAnimationMessage(message) {
    if (!routeAnimationMessage) {
      return;
    }

    routeAnimationMessage.hidden = !message;
    setLocalizedText(routeAnimationMessage, message || "");
  }

  function updateRouteAnimationButton() {
    if (!startRouteButton) {
      return;
    }

    var selectedRobot = selectedRobotKey ? robotDefinitions[selectedRobotKey] : null;
    var isFollowingBackend = isBackendExecutionActive(selectedRobot);
    var canAnimate = Boolean(selectedRobotKey && hasRobotMissionTarget(selectedRobotKey) && !routeAnimating && !isFollowingBackend);

    startRouteButton.disabled = !canAnimate;

    if (isFollowingBackend) {
      setLocalizedText(startRouteButton, "Following Backend State");
      return;
    }

    setLocalizedText(startRouteButton, routeAnimating ? "Visual Preview Running" : "Visual Route Preview");
  }

  function createSvgElement(tagName, attributes) {
    var element = document.createElementNS(svgNamespace, tagName);

    Object.keys(attributes).forEach(function (name) {
      element.setAttribute(name, attributes[name]);
    });

    return element;
  }

  function toRoutePosition(x, y) {
    return {
      left: Number((x / routeCanvasWidth * 100).toFixed(2)),
      top: Number((y / routeCanvasHeight * 100).toFixed(2))
    };
  }

  function toRouteWorldPosition(zoneName, localPosition) {
    var layout = routeWorldZoneLayout[zoneName];

    if (!layout || !localPosition) {
      return null;
    }

    return {
      left: Number(((layout.left + localPosition.left / 100 * layout.width) / routeWorldWidth * 100).toFixed(2)),
      top: Number(((layout.top + localPosition.top / 100 * layout.height) / routeWorldHeight * 100).toFixed(2))
    };
  }

  function toLocalZonePosition(zoneName, worldPosition) {
    var layout = routeWorldZoneLayout[zoneName];

    if (!layout || !worldPosition) {
      return null;
    }

    return {
      left: Number((((worldPosition.left / 100 * routeWorldWidth) - layout.left) / layout.width * 100).toFixed(2)),
      top: Number((((worldPosition.top / 100 * routeWorldHeight) - layout.top) / layout.height * 100).toFixed(2))
    };
  }

  function createRouteWaypointDefinition(zoneName, position) {
    return {
      zoneName: zoneName,
      position: clonePosition(position)
    };
  }

  function aliasRouteWaypoint(waypoints, aliasName, sourceName) {
    var source = waypoints[sourceName];

    if (source) {
      waypoints[aliasName] = createRouteWaypointDefinition(source.zoneName, source.position);
    }
  }

  function buildRouteWorldWaypoints() {
    var waypoints = {
      "base-station": createRouteWaypointDefinition("c", baseStationPosition),
      "charging-station": createRouteWaypointDefinition("c", chargingStationPosition)
    };

    ["a", "b", "c"].forEach(function (zoneName) {
      routeNodes.forEach(function (node) {
        var routePointName = getRoutePointNameForNode(zoneName, node.key);
        waypoints[routePointName] = createRouteWaypointDefinition(
          zoneName,
          toRouteWorldPosition(zoneName, toRoutePosition(node.x, node.y))
        );
      });
    });

    waypoints["bridge-c-b-1"] = createRouteWaypointDefinition("c", { left: 36.67, top: 65.22 });
    waypoints["bridge-c-b-2"] = createRouteWaypointDefinition("b", { left: 36.67, top: 60.33 });
    waypoints["bridge-b-a-1"] = createRouteWaypointDefinition("b", { left: 36.67, top: 31.52 });
    waypoints["bridge-b-a-2"] = createRouteWaypointDefinition("a", { left: 36.67, top: 26.63 });
    waypoints["bridge-b-c-1"] = createRouteWaypointDefinition("b", { left: 36.67, top: 60.33 });
    waypoints["bridge-b-c-2"] = createRouteWaypointDefinition("c", { left: 36.67, top: 65.22 });
    waypoints["bridge-a-b-1"] = createRouteWaypointDefinition("a", { left: 36.67, top: 26.63 });
    waypoints["bridge-a-b-2"] = createRouteWaypointDefinition("b", { left: 36.67, top: 31.52 });

    ["a", "b", "c"].forEach(function (zoneName) {
      aliasRouteWaypoint(waypoints, "zone-" + zoneName + "-left-entry", "zone-" + zoneName + "-entry");
      aliasRouteWaypoint(waypoints, "zone-" + zoneName + "-left-main-1", "zone-" + zoneName + "-main-1");
      aliasRouteWaypoint(waypoints, "zone-" + zoneName + "-left-main-2", "zone-" + zoneName + "-main-2");
      aliasRouteWaypoint(waypoints, "zone-" + zoneName + "-right-main-2", "zone-" + zoneName + "-upper-cross");
      aliasRouteWaypoint(waypoints, "zone-" + zoneName + "-right-main-1", "zone-" + zoneName + "-middle-cross");
      aliasRouteWaypoint(waypoints, "zone-" + zoneName + "-right-exit", "zone-" + zoneName + "-lower-cross");
    });

    waypoints["bridge-c-b-left-1"] = createRouteWaypointDefinition("c", { left: OUTBOUND_COLUMN_LEFT, top: 65.22 });
    waypoints["bridge-c-b-left-2"] = createRouteWaypointDefinition("b", { left: OUTBOUND_COLUMN_LEFT, top: 60.33 });
    waypoints["bridge-b-a-left-1"] = createRouteWaypointDefinition("b", { left: OUTBOUND_COLUMN_LEFT, top: 31.52 });
    waypoints["bridge-b-a-left-2"] = createRouteWaypointDefinition("a", { left: OUTBOUND_COLUMN_LEFT, top: 26.63 });
    waypoints["bridge-a-b-right-1"] = createRouteWaypointDefinition("a", { left: RETURN_COLUMN_LEFT, top: 26.63 });
    waypoints["bridge-a-b-right-2"] = createRouteWaypointDefinition("b", { left: RETURN_COLUMN_LEFT, top: 31.52 });
    waypoints["bridge-b-c-right-1"] = createRouteWaypointDefinition("b", { left: RETURN_COLUMN_LEFT, top: 60.33 });
    waypoints["bridge-b-c-right-2"] = createRouteWaypointDefinition("c", { left: RETURN_COLUMN_LEFT, top: 65.22 });
    addTargetApproachWaypoints(waypoints);

    return waypoints;
  }

  function getTargetApproachLocalPosition(slotPosition) {
    if (!slotPosition) {
      return null;
    }

    return {
      left: OUTBOUND_COLUMN_LEFT,
      top: slotPosition.top
    };
  }

  function getTargetRowRouteLocalPosition(locationCode) {
    var rowIndex = getTargetRowIndex(locationCode);
    var routeYByRow = {
      1: 95,
      2: 215,
      3: 335
    };

    return toRoutePosition(330, routeYByRow[rowIndex] || routeYByRow[3]);
  }

  function toSvgCoordinate(value, size) {
    return Number((value / 100 * size).toFixed(2));
  }

  function toSvgX(position) {
    return toSvgCoordinate(position.left, routeCanvasWidth);
  }

  function toSvgY(position) {
    return toSvgCoordinate(position.top, routeCanvasHeight);
  }

  function addTargetApproachWaypoints(waypoints) {
    Object.keys(locationCoordinates).forEach(function (zoneName) {
      var zoneLocations = locationCoordinates[zoneName] || {};

      Object.keys(zoneLocations).forEach(function (locationCode) {
        var approachPosition = getTargetApproachLocalPosition(zoneLocations[locationCode]);

        if (approachPosition) {
          waypoints[locationCode + "-approach"] = createRouteWaypointDefinition(
            zoneName,
            toRouteWorldPosition(zoneName, approachPosition)
          );
        }
      });
    });
  }

  function getRoutePointNameForNode(zoneName, nodeKey) {
    if (nodeKey === "lower-main") {
      return "zone-" + zoneName + "-entry";
    }

    if (nodeKey === "middle-main") {
      return "zone-" + zoneName + "-main-1";
    }

    if (nodeKey === "upper-main") {
      return "zone-" + zoneName + "-main-2";
    }

    return "zone-" + zoneName + "-" + nodeKey;
  }

  function getBridgeRoutePointName(zoneName) {
    if (zoneName === "c") {
      return "bridge-c-b-left-1";
    }

    if (zoneName === "b") {
      return "bridge-b-a-left-1";
    }

    return "";
  }

  function clonePosition(position) {
    return { left: position.left, top: position.top };
  }

  function isSamePosition(first, second) {
    return first && second && first.left === second.left && first.top === second.top;
  }

  function almostSamePosition(first, second) {
    return first && second && Math.abs(first.left - second.left) < 0.01 && Math.abs(first.top - second.top) < 0.01;
  }

  function interpolatePosition(startPosition, targetPosition, progress) {
    return {
      left: Number((startPosition.left + (targetPosition.left - startPosition.left) * progress).toFixed(2)),
      top: Number((startPosition.top + (targetPosition.top - startPosition.top) * progress).toFixed(2))
    };
  }

  function clampSegmentProgress(value) {
    var numericValue = Number(value);

    if (!Number.isFinite(numericValue)) {
      return 0;
    }

    return Math.max(0, Math.min(1, numericValue));
  }

  function normalizeBatteryPercent(value) {
    var numericValue = Number(value);

    if (!Number.isFinite(numericValue)) {
      return null;
    }

    return Math.max(0, Math.min(100, Math.round(numericValue)));
  }

  function normalizeBatteryWarningLevel(value) {
    var warningLevel = value ? String(value).trim().toUpperCase() : "NORMAL";

    return warningLevel === "LOW" || warningLevel === "CRITICAL" ? warningLevel : "NORMAL";
  }

  function getInterpolatedRobotVisualPosition(robotKey, timestamp) {
    var state = robotVisualPositionState[robotKey];
    var elapsed;
    var progress;

    if (!state) {
      return null;
    }

    if (!state.startedAt || state.durationMs <= 0) {
      return clonePosition(state.targetPosition);
    }

    elapsed = timestamp - state.startedAt;
    progress = Math.max(0, Math.min(1, elapsed / state.durationMs));

    if (progress >= 1) {
      state.currentPosition = clonePosition(state.targetPosition);
      state.startedAt = 0;
      state.durationMs = 0;
      return clonePosition(state.targetPosition);
    }

    return interpolatePosition(state.currentPosition, state.targetPosition, progress);
  }

  function isBridgePositionKey(positionKey) {
    return normalizePositionKey(positionKey).indexOf("bridge-") === 0;
  }

  function getRobotVisualMovementDurationMs(robotKey) {
    var robot = robotDefinitions[robotKey];
    var state = robotVisualPositionState[robotKey];
    var targetPositionKey = robot ? normalizePositionKey(robot.currentPositionKey) : "";
    var nextPositionKey = robot ? normalizePositionKey(robot.nextPositionKey || "") : "";

    if (robot && nextPositionKey) {
      return ROBOT_VISUAL_SEGMENT_SAMPLE_DURATION_MS;
    }

    if (isBridgePositionKey(targetPositionKey) || isBridgePositionKey(nextPositionKey) ||
        (state && isBridgePositionKey(state.targetPositionKey))) {
      return ROBOT_VISUAL_BRIDGE_MOVEMENT_DURATION_MS;
    }

    return ROBOT_VISUAL_MOVEMENT_DURATION_MS;
  }

  function getRobotVisualRenderPosition(robotKey, zoneName, targetPosition) {
    var timestamp = Date.now();
    var state = robotVisualPositionState[robotKey];
    var currentVisualPosition;
    var durationMs;
    var robot;

    if (!targetPosition) {
      return null;
    }

    robot = robotDefinitions[robotKey];

    if (!state || state.zoneName !== zoneName) {
      robotVisualPositionState[robotKey] = {
        zoneName: zoneName,
        currentPosition: clonePosition(targetPosition),
        targetPosition: clonePosition(targetPosition),
        targetPositionKey: robot
          ? normalizePositionKey(robot.currentPositionKey)
            + "->"
            + normalizePositionKey(robot.nextPositionKey || "")
          : "",
        startedAt: 0,
        durationMs: 0
      };
      return clonePosition(targetPosition);
    }

    currentVisualPosition = getInterpolatedRobotVisualPosition(robotKey, timestamp) || clonePosition(targetPosition);

    if (almostSamePosition(state.targetPosition, targetPosition)) {
      return currentVisualPosition;
    }

    durationMs = getRobotVisualMovementDurationMs(robotKey);
    robotVisualPositionState[robotKey] = {
      zoneName: zoneName,
      currentPosition: currentVisualPosition,
      targetPosition: clonePosition(targetPosition),
      targetPositionKey: robot
        ? normalizePositionKey(robot.currentPositionKey)
          + "->"
          + normalizePositionKey(robot.nextPositionKey || "")
        : "",
      startedAt: timestamp,
      durationMs: durationMs
    };

    return currentVisualPosition;
  }

  function getRobotVisualTargetPosition(robotKey) {
    var state = robotVisualPositionState[robotKey];

    return state ? clonePosition(state.targetPosition) : null;
  }

  function isRobotVisualAnimationActive(robotKey) {
    var state = robotVisualPositionState[robotKey];

    return Boolean(state && state.startedAt && state.durationMs > 0);
  }

  function applyMarkerCssPosition(marker, position) {
    marker.style.setProperty("--robot-left", position.left + "%");
    marker.style.setProperty("--robot-top", position.top + "%");
  }

  function animateRobotMarkerToPosition(marker) {
    var robotKey = marker.dataset.robotMarker;
    var targetPosition = getRobotVisualTargetPosition(robotKey);
    var visualPosition = getInterpolatedRobotVisualPosition(robotKey, Date.now()) || targetPosition;

    if (!visualPosition) {
      return;
    }

    marker.classList.toggle("is-smoothing", isRobotVisualAnimationActive(robotKey));
    applyMarkerCssPosition(marker, visualPosition);
  }

  function updateRobotMarkerAnimationFrame() {
    var hasActiveMarkerAnimation = false;

    activeZoneMap.querySelectorAll(".map-robot-marker[data-robot-marker]:not(.route-animation-robot)").forEach(function (marker) {
      var robotKey = marker.dataset.robotMarker;

      animateRobotMarkerToPosition(marker);
      if (isRobotVisualAnimationActive(robotKey)) {
        hasActiveMarkerAnimation = true;
      }
    });

    if (hasActiveMarkerAnimation) {
      robotMarkerAnimationFrame = window.requestAnimationFrame(updateRobotMarkerAnimationFrame);
      return;
    }

    robotMarkerAnimationFrame = null;
  }

  function scheduleRobotMarkerAnimations() {
    if (robotMarkerAnimationFrame) {
      window.cancelAnimationFrame(robotMarkerAnimationFrame);
    }

    robotMarkerAnimationFrame = window.requestAnimationFrame(updateRobotMarkerAnimationFrame);
  }

  function addRouteWaypoint(route, position, stepKey, zoneName, routePointName, locationCode) {
    var lastWaypoint = route[route.length - 1];

    if (!position) {
      return;
    }

    if (lastWaypoint
        && isSamePosition(lastWaypoint.position, position)
        && lastWaypoint.zoneName === zoneName
        && lastWaypoint.stepKey === stepKey) {
      return;
    }

    route.push({
      position: clonePosition(position),
      stepKey: stepKey,
      zoneName: zoneName,
      routePoint: routePointName || "",
      locationCode: locationCode || "",
      key: routePointName || locationCode || ""
    });
  }

  function addNamedRouteWaypoint(route, routePointName, stepKey) {
    var waypoint = routeWaypoints[routePointName];

    if (!waypoint) {
      return;
    }

    addRouteWaypoint(route, waypoint.position, stepKey, waypoint.zoneName, routePointName, "");
  }

  function getTargetPosition(zoneName, locationCode) {
    var zoneLocations = locationCoordinates[zoneName] || {};
    var normalizedLocationCode = normalizeLocationCode(locationCode);
    var localPosition = zoneLocations[normalizedLocationCode];

    return localPosition ? toRouteWorldPosition(zoneName, localPosition) : null;
  }

  function getZoneFromLocationCode(locationCode) {
    var normalizedLocationCode = normalizeLocationCode(locationCode);
    var zoneKey = normalizedLocationCode.charAt(0).toLowerCase();

    return zoneData[zoneKey] ? zoneKey : defaultZone;
  }

  function getTargetRowIndex(locationCode) {
    var slotNumber = parseInt(normalizeLocationCode(locationCode).slice(1), 10);

    if (!slotNumber || slotNumber < 1 || slotNumber > 9) {
      return 0;
    }

    return Math.ceil(slotNumber / 3);
  }

  function buildOutboundTargetRowRoutePointNames(zoneName, locationCode) {
    var rowIndex = getTargetRowIndex(locationCode);
    var pointNames = [];

    if (!rowIndex) {
      return pointNames;
    }

    if (rowIndex === 1) {
      pointNames.push("zone-" + zoneName + "-left-main-1");
      pointNames.push("zone-" + zoneName + "-left-main-2");
    }
    if (rowIndex === 2) {
      pointNames.push("zone-" + zoneName + "-left-main-1");
    }

    return pointNames;
  }

  function buildReturnTargetRowRoutePointNames(zoneName, locationCode) {
    var rowIndex = getTargetRowIndex(locationCode);
    var pointNames = [];

    if (!rowIndex) {
      return pointNames;
    }

    if (rowIndex === 1) {
      pointNames.push("zone-" + zoneName + "-right-main-2");
      pointNames.push("zone-" + zoneName + "-right-main-1");
    }
    if (rowIndex === 2) {
      pointNames.push("zone-" + zoneName + "-right-main-1");
    }

    return pointNames;
  }

  function buildRouteToTarget(locationCode) {
    var normalizedLocationCode = normalizeLocationCode(locationCode);
    var zoneName = getZoneFromLocationCode(normalizedLocationCode);
    var targetPosition = getTargetPosition(zoneName, locationCode);
    var routePointNames = outboundRoutePointSequences[zoneName] || outboundRoutePointSequences[defaultZone];
    var route = [];

    routePointNames.forEach(function (routePointName) {
      addNamedRouteWaypoint(route, routePointName, "move");
    });
    buildOutboundTargetRowRoutePointNames(zoneName, normalizedLocationCode).forEach(function (routePointName) {
      addNamedRouteWaypoint(route, routePointName, "move");
    });

    addNamedRouteWaypoint(route, normalizedLocationCode + "-approach", "move");
    addRouteWaypoint(route, targetPosition, "pickup", zoneName, "target:" + normalizedLocationCode, normalizedLocationCode);

    return route;
  }

  function buildReturnRoute(locationCode) {
    var normalizedLocationCode = normalizeLocationCode(locationCode);
    var zoneName = getZoneFromLocationCode(normalizedLocationCode);
    var targetPosition = getTargetPosition(zoneName, locationCode);
    var routePointNames = returnRoutePointSequences[zoneName] || returnRoutePointSequences[defaultZone];
    var route = [];

    addRouteWaypoint(route, targetPosition, "return", zoneName, "target:" + normalizedLocationCode, normalizedLocationCode);
    addNamedRouteWaypoint(route, normalizedLocationCode + "-approach", "return");
    buildReturnTargetRowRoutePointNames(zoneName, normalizedLocationCode).forEach(function (routePointName) {
      addNamedRouteWaypoint(route, routePointName, "return");
    });

    routePointNames.forEach(function (routePointName) {
      addNamedRouteWaypoint(route, routePointName, "return");
    });

    return route;
  }

  function buildPickupRoute(robotKey, zoneName) {
    var robot = robotDefinitions[robotKey];
    var goRoute = buildRouteToTarget(robot ? robot.locationCode : "");
    var returnRoute = buildReturnRoute(robot ? robot.locationCode : "");

    return goRoute.concat(returnRoute.slice(1));
  }

  function resolveWorldPositionAnchor(currentPositionKey, targetLocationCode) {
    var positionKey = normalizePositionKey(currentPositionKey);
    var targetKey = normalizeLocationCode(targetLocationCode || "");
    var slotZoneName;
    var routeWaypoint;

    if (/^[ABC][1-9]$/.test(positionKey)) {
      slotZoneName = getZoneFromLocationCode(positionKey);

      if (locationCoordinates[slotZoneName] && locationCoordinates[slotZoneName][positionKey]) {
        return {
          zoneName: slotZoneName,
          position: toRouteWorldPosition(slotZoneName, locationCoordinates[slotZoneName][positionKey]),
          anchorType: "data-location",
          anchorKey: positionKey,
          locationCode: positionKey
        };
      }
    }

    routeWaypoint = routeWaypoints[positionKey];

    if (routeWaypoint) {
      return {
        zoneName: routeWaypoint.zoneName,
        position: clonePosition(routeWaypoint.position),
        anchorType: "data-route-point",
        anchorKey: positionKey,
        locationCode: ""
      };
    }

    if (targetKey) {
      slotZoneName = getZoneFromLocationCode(targetKey);

      if (locationCoordinates[slotZoneName] && locationCoordinates[slotZoneName][targetKey]) {
        return {
          zoneName: slotZoneName,
          position: toRouteWorldPosition(slotZoneName, locationCoordinates[slotZoneName][targetKey]),
          anchorType: "data-location",
          anchorKey: targetKey,
          locationCode: targetKey
        };
      }
    }

    routeWaypoint = routeWaypoints["base-station"];
    return {
      zoneName: routeWaypoint.zoneName,
      position: clonePosition(routeWaypoint.position),
      anchorType: "data-route-point",
      anchorKey: "base-station",
      locationCode: ""
    };
  }

  function toLocalPositionAnchor(worldAnchor) {
    var localRoutePosition;

    if (!worldAnchor || !worldAnchor.position) {
      return resolvePositionAnchor("base-station", "");
    }

    localRoutePosition = toLocalZonePosition(worldAnchor.zoneName, worldAnchor.position);

    return {
      zoneName: worldAnchor.zoneName,
      position: localRoutePosition,
      anchorType: worldAnchor.anchorType,
      anchorKey: worldAnchor.anchorKey,
      locationCode: worldAnchor.locationCode || ""
    };
  }

  function resolvePositionAnchor(currentPositionKey, targetLocationCode) {
    return toLocalPositionAnchor(resolveWorldPositionAnchor(currentPositionKey, targetLocationCode));
  }

  function resolveSegmentPositionAnchor(currentPositionKey, nextPositionKey, segmentProgress, targetLocationCode) {
    var currentAnchor = resolveWorldPositionAnchor(currentPositionKey, targetLocationCode);
    var normalizedNextPositionKey = normalizePositionKey(nextPositionKey || "");
    var nextAnchor;
    var interpolatedAnchor;
    var progress;

    if (!normalizedNextPositionKey) {
      return toLocalPositionAnchor(currentAnchor);
    }

    nextAnchor = resolveWorldPositionAnchor(normalizedNextPositionKey, targetLocationCode);
    progress = clampSegmentProgress(segmentProgress);
    interpolatedAnchor = {
      zoneName: progress < 0.5 ? currentAnchor.zoneName : nextAnchor.zoneName,
      position: interpolatePosition(currentAnchor.position, nextAnchor.position, progress),
      anchorType: "data-route-point",
      anchorKey: currentAnchor.anchorKey + "->" + nextAnchor.anchorKey,
      locationCode: ""
    };

    return toLocalPositionAnchor(interpolatedAnchor);
  }

  function resolveRobotKeyFromState(robotState) {
    var robotText = [
      robotState.robotName || "",
      robotState.robotCode || "",
      robotState.color || ""
    ].join(" ").toLowerCase();

    if (robotText.indexOf("alpha") >= 0 || robotText.indexOf("picker") >= 0 ||
        robotText.indexOf("rb-100") >= 0 || robotText.indexOf("green") >= 0) {
      return "picker";
    }

    if (robotText.indexOf("beta") >= 0 || robotText.indexOf("mover") >= 0 ||
        robotText.indexOf("rb-200") >= 0 || robotText.indexOf("red") >= 0) {
      return "mover";
    }

    if (robotText.indexOf("gamma") >= 0 || robotText.indexOf("carrier") >= 0 ||
        robotText.indexOf("rb-300") >= 0 || robotText.indexOf("blue") >= 0) {
      return "carrier";
    }

    return "";
  }

  function getTargetText(robot) {
    if (!robot || !robot.targetLocationCode) {
      return "assigned target";
    }

    return (robot.targetZone || zoneData[getZoneFromLocationCode(robot.targetLocationCode)].label) + " - " + robot.targetLocationCode;
  }

  function executionStepToFlowStep(executionStep) {
    if (executionStep === "MOVING_TO_TARGET") {
      return "move";
    }

    if (executionStep === "PICKING_UP") {
      return "pickup";
    }

    if (executionStep === "RETURNING_TO_BASE") {
      return "return";
    }

    if (executionStep === "RETURNED_TO_BASE") {
      return "returned";
    }

    return "assigned";
  }

  function formatStrategyName(strategyName) {
    var normalizedName = strategyName ? String(strategyName).trim() : "";

    if (normalizedName === "NormalStrategy") {
      return "Normal";
    }

    return normalizedName || "No active strategy";
  }

  function formatUserMessage(message) {
    var rawMessage = message ? String(message) : "";
    var lowerMessage = rawMessage.toLowerCase();

    if (!rawMessage) {
      return "";
    }

    if (lowerMessage.indexOf("charging at charging station") >= 0) {
      return "Charging at station.";
    }

    if (lowerMessage.indexOf("charging required after current mission") >= 0) {
      return "Charging required after this task.";
    }

    if (lowerMessage.indexOf("waiting for path to clear") >= 0 ||
        lowerMessage.indexOf("waiting for bridge path to clear") >= 0) {
      return "Waiting for path to clear.";
    }

    if (lowerMessage.indexOf("returned to base station") >= 0 ||
        lowerMessage.indexOf("robot returned to base station") >= 0) {
      return "Returned to Base. Waiting for confirmation.";
    }

    if (lowerMessage.indexOf("large cargo picked up") >= 0 ||
        lowerMessage.indexOf("heavy load") >= 0) {
      return "Heavy load mode active.";
    }

    return rawMessage;
  }

  function createCurrentTargetBox(robot) {
    var targetBox = document.createElement("div");
    var label = document.createElement("span");
    var value = document.createElement("strong");

    targetBox.className = "current-target-box";
    setLocalizedText(label, "Current Target");
    setLocalizedText(value, "Move to " + getTargetText(robot));

    targetBox.appendChild(label);
    targetBox.appendChild(value);
    targetBox.appendChild(createBatteryStatusLine(robot));
    appendStrategyStatusLine(targetBox, robot);

    return targetBox;
  }

  function createBatteryStatusBox(robot) {
    var targetBox = document.createElement("div");
    var label = document.createElement("span");

    targetBox.className = "current-target-box battery-summary-box";
    setLocalizedText(label, "Robot Battery");

    targetBox.appendChild(label);
    targetBox.appendChild(createBatteryStatusLine(robot));
    appendStrategyStatusLine(targetBox, robot);

    return targetBox;
  }

  function appendStrategyStatusLine(container, robot) {
    var strategyLine = createStrategyStatusLine(robot);

    if (strategyLine) {
      container.appendChild(strategyLine);
    }
  }

  function createStrategyStatusLine(robot) {
    var primaryStrategyName = robot ? robot.primaryStrategyName || "" : "";
    var currentActiveStrategyName = robot ? robot.currentActiveStrategyName || "" : "";
    var strategyMessage = robot ? robot.strategyMessage || "" : "";
    var row;
    var label;
    var valueGroup;
    var primaryText;
    var activeText;
    var primaryLabel;
    var activeLabel;

    if (!primaryStrategyName && !currentActiveStrategyName) {
      return null;
    }
    if (!robot.charging && !robot.missionId && primaryStrategyName === "NormalStrategy") {
      return null;
    }

    row = document.createElement("div");
    label = document.createElement("span");
    valueGroup = document.createElement("div");
    primaryText = document.createElement("small");
    activeText = document.createElement("strong");

    row.className = "strategy-status-line";
    setLocalizedText(label, "Strategy");
    valueGroup.className = "strategy-status-value";
    primaryText.className = "strategy-badge strategy-primary-text";
    activeText.className = "strategy-badge is-active strategy-active-text";
    primaryLabel = formatStrategyName(primaryStrategyName);
    activeLabel = formatStrategyName(currentActiveStrategyName || primaryStrategyName);
    setLocalizedText(primaryText, "Primary Strategy: " + primaryLabel);
    primaryText.title = primaryStrategyName || primaryLabel;
    setLocalizedText(activeText, "Active Strategy: " + activeLabel);
    activeText.title = currentActiveStrategyName || primaryStrategyName || activeLabel;

    valueGroup.appendChild(primaryText);
    valueGroup.appendChild(activeText);

    if (strategyMessage) {
      var note = document.createElement("small");
      note.className = "strategy-message-text";
      setLocalizedText(note, formatUserMessage(strategyMessage));
      valueGroup.appendChild(note);
    }

    row.appendChild(label);
    row.appendChild(valueGroup);
    return row;
  }

  function createBatteryStatusLine(robot) {
    var row = document.createElement("div");
    var label = document.createElement("span");
    var valueGroup = document.createElement("div");
    var value = document.createElement("strong");
    var batteryMessage;
    var movementModeText;

    row.className = "battery-status-line";
    row.classList.toggle("is-low", Boolean(robot && robot.lowBattery));
    row.classList.toggle("is-critical", Boolean(robot && robot.criticalBattery));
    setLocalizedText(label, "Battery");
    valueGroup.className = "battery-status-value";
    setLocalizedText(value, robot.batteryDisplayText || formatBatteryDisplayText(robot));

    row.appendChild(label);
    valueGroup.appendChild(value);
    valueGroup.appendChild(createBatteryMiniProgress(robot));

    if (robot && (robot.charging || robot.batteryWarningLevel !== "NORMAL")) {
      valueGroup.appendChild(createBatteryWarningBadge(robot));
    }

    batteryMessage = robot && (robot.charging || robot.batteryWarningLevel !== "NORMAL") ? robot.batteryMessage : "";
    if (batteryMessage) {
      var note = document.createElement("small");
      note.className = "battery-message-text";
      setLocalizedText(note, formatUserMessage(batteryMessage));
      valueGroup.appendChild(note);
    }

    movementModeText = formatMovementModeText(robot);
    if (movementModeText) {
      var modeNote = document.createElement("small");
      modeNote.className = "movement-mode-text";
      setLocalizedText(modeNote, movementModeText);
      valueGroup.appendChild(modeNote);
    }

    row.appendChild(valueGroup);

    return row;
  }

  function createBatteryMiniProgress(robot) {
    var track = document.createElement("div");
    var bar = document.createElement("div");
    var batteryPercent = normalizeBatteryPercent(robot ? robot.batteryPercent : null);

    track.className = "battery-mini-track";
    bar.className = "battery-mini-bar";
    bar.classList.toggle("is-low", Boolean(robot && robot.lowBattery && !robot.criticalBattery));
    bar.classList.toggle("is-critical", Boolean(robot && robot.criticalBattery));
    bar.classList.toggle("is-charging", Boolean(robot && robot.charging));
    bar.style.width = (batteryPercent === null ? 0 : batteryPercent) + "%";

    track.appendChild(bar);
    return track;
  }

  function createBatteryWarningBadge(robot) {
    var badge = document.createElement("span");

    badge.className = "battery-warning-badge";
    badge.classList.toggle("is-charging", Boolean(robot && robot.charging));
    badge.classList.toggle("is-critical", Boolean(robot && robot.criticalBattery));
    badge.classList.toggle("is-low", Boolean(robot && robot.lowBattery && !robot.criticalBattery));
    setLocalizedText(badge, robot && robot.charging
      ? "Charging"
      : (robot && robot.criticalBattery ? "Critical Battery" : "Energy Saving"));

    return badge;
  }

  function formatBatteryDisplayText(robot) {
    var batteryPercent = normalizeBatteryPercent(robot ? robot.batteryPercent : null);
    var batteryDrainPercent = Math.max(0, Number(robot && robot.batteryDrainPercent ? robot.batteryDrainPercent : 0));

    if (batteryPercent === null) {
      return "Battery unavailable";
    }

    if (batteryDrainPercent > 0) {
      return batteryPercent + "% battery (" + batteryDrainPercent + "% route drain)";
    }

    return batteryPercent + "% battery";
  }

  function formatMovementModeText(robot) {
    if (!robot || robot.charging || !robot.movementModeDisplay) {
      return "";
    }

    return robot.movementModeDisplay;
  }

  function createMissionFlowStep(index, stepKey, title, description) {
    var item = document.createElement("li");
    var stepIndex = document.createElement("span");
    var body = document.createElement("span");
    var strong = document.createElement("strong");

    item.className = "flow-step";
    item.dataset.flowStep = stepKey;

    stepIndex.className = "flow-step-index";
    stepIndex.textContent = String(index);

    body.className = "flow-step-body";
    setLocalizedText(strong, title);

    body.appendChild(strong);

    if (description) {
      var small = document.createElement("small");
      setLocalizedText(small, description);
      body.appendChild(small);
    }

    item.appendChild(stepIndex);
    item.appendChild(body);

    return item;
  }

  function createRobotStatusOverview(robot) {
    var overview = document.createElement("div");
    var missionLabel = document.createElement("span");
    var missionValue = document.createElement("strong");
    var statusLabel = document.createElement("span");
    var statusValue = document.createElement("strong");

    overview.className = "robot-status-overview";

    setLocalizedText(missionLabel, "Current Mission");
    setLocalizedText(missionValue, robot && robot.requestCode ? robot.requestCode : "None");
    setLocalizedText(statusLabel, "Status");
    setLocalizedText(statusValue, robot && (robot.robotStatus || robot.missionStatus)
      ? (robot.robotStatus || robot.missionStatus)
      : "IDLE");

    overview.appendChild(missionLabel);
    overview.appendChild(missionValue);
    overview.appendChild(statusLabel);
    overview.appendChild(statusValue);

    return overview;
  }

  function createPickupFlowTree(robot) {
    var list = document.createElement("ol");
    var targetText = getTargetText(robot);

    list.className = "pickup-flow-tree";
    list.appendChild(createMissionFlowStep(1, "assigned", "Assigned", robot.requestCode || ""));
    list.appendChild(createMissionFlowStep(2, "move", "Moving", "Move to " + targetText));
    list.appendChild(createMissionFlowStep(3, "pickup", "Pickup", robot.targetLocationCode || ""));
    list.appendChild(createMissionFlowStep(4, "return", "Returning", "To Base Station"));
    list.appendChild(createMissionFlowStep(5, "returned", "Returned / Waiting Confirmation", "Waiting for confirmation"));

    return list;
  }

  function createMapEmptyState(message) {
    var emptyState = document.createElement("div");

    emptyState.className = "map-empty-state";
    setLocalizedText(emptyState, message);

    return emptyState;
  }

  function renderMissionFlowCard(robotKey) {
    var card = document.querySelector("[data-robot-flow='" + robotKey + "']");
    var robot = robotDefinitions[robotKey];

    if (!card || !robot) {
      return;
    }

    card.querySelectorAll(".robot-status-overview, .current-target-box, .map-empty-state, .pickup-flow-tree").forEach(function (element) {
      element.remove();
    });

    card.appendChild(createRobotStatusOverview(robot));

    if (!robot.hasActiveMission) {
      card.appendChild(createMapEmptyState(
        robot.charging
          ? formatUserMessage(robot.message || "Charging at Charging Station.")
          : "No active pickup mission assigned."
      ));
      card.appendChild(createBatteryStatusBox(robot));
      translateFragment(card);
      return;
    }

    card.appendChild(createCurrentTargetBox(robot));
    card.appendChild(createPickupFlowTree(robot));
    updateMissionFlowStep(robotKey, executionStepToFlowStep(robot.executionStep));
    translateFragment(card);
  }

  function getExecutionMessage(robot) {
    if (robot && robot.charging) {
      return formatUserMessage(robot.message || "Charging at Charging Station.");
    }

    if (!robot || !robot.hasActiveMission) {
      return "No active pickup mission assigned.";
    }

    if (robot.waiting) {
      return "Waiting for path to clear.";
    }

    if (robot.executionStep === "RETURNED_TO_BASE") {
      return "Returned to Base. Waiting for confirmation.";
    }

    if (robot.message) {
      return formatUserMessage(robot.message);
    }

    if (robot.executionStep === "MOVING_TO_TARGET") {
      return "Moving to target location.";
    }

    if (robot.executionStep === "PICKING_UP") {
      return "Picking up cargo.";
    }

    if (robot.executionStep === "RETURNING_TO_BASE") {
      return "Returning to Base Station.";
    }

    return "Mission assigned. Waiting for Staff to start execution.";
  }

  function renderSelectedRobotStateMessage(robotKey) {
    var robot = robotDefinitions[robotKey];

    if (!robot) {
      return;
    }

    setRouteAnimationMessage(getExecutionMessage(robot));
  }

  function updateRobotControlState(robotKey) {
    var robot = robotDefinitions[robotKey];

    if (!robot) {
      return;
    }

    robotControlButtons.forEach(function (button) {
      if (button.dataset.robotSelect !== robotKey) {
        return;
      }

      button.dataset.currentZone = getRobotZone(robotKey);
      button.dataset.locationCode = robot.targetLocationCode || "";
    });
  }

  function applyRobotState(robotState) {
    var robotKey = resolveRobotKeyFromState(robotState);
    var robot = robotDefinitions[robotKey];
    var targetLocationCode;

    if (!robot) {
      return;
    }

    targetLocationCode = normalizeLocationCode(robotState.targetLocationCode || "");

    robot.backendState = robotState;
    robot.missionId = robotState.missionId || null;
    robot.requestCode = robotState.requestCode || "";
    robot.hasActiveMission = Boolean(robotState.missionId);
    robot.missionStatus = robotState.status || "";
    robot.executionStep = robotState.executionStep || "";
    robot.currentPositionKey = normalizePositionKey(robotState.currentPositionKey || "");
    robot.nextPositionKey = normalizePositionKey(robotState.nextPositionKey || "");
    robot.segmentProgress = clampSegmentProgress(robotState.segmentProgress);
    robot.batteryLevel = normalizeBatteryPercent(robotState.batteryLevel);
    robot.batteryPercent = normalizeBatteryPercent(robotState.batteryPercent);
    robot.batteryDrainPercent = Math.max(0, Number(robotState.batteryDrainPercent || 0));
    robot.batteryDisplayText = robotState.batteryDisplayText || formatBatteryDisplayText(robot);
    robot.batteryWarningLevel = normalizeBatteryWarningLevel(robotState.batteryWarningLevel);
    robot.lowBattery = Boolean(robotState.lowBattery);
    robot.criticalBattery = Boolean(robotState.criticalBattery);
    robot.energySavingMode = Boolean(robotState.energySavingMode);
    robot.chargingRequired = Boolean(robotState.chargingRequired);
    robot.batteryMessage = robotState.batteryMessage || "";
    robot.charging = Boolean(robotState.charging);
    robot.chargingRecoveredPercent = Math.max(0, Number(robotState.chargingRecoveredPercent || 0));
    robot.chargingDisplayText = robotState.chargingDisplayText || "";
    robot.movementMode = robotState.movementMode || "";
    robot.movementModeDisplay = robotState.movementModeDisplay || "";
    robot.waypointsPerBatteryPercent = Number(robotState.waypointsPerBatteryPercent || 0);
    robot.batteryDrainMode = robotState.batteryDrainMode || "";
    robot.primaryStrategyName = robotState.primaryStrategyName || "";
    robot.currentActiveStrategyName = robotState.currentActiveStrategyName || "";
    robot.strategyMessage = robotState.strategyMessage || "";
    robot.robotStatus = robotState.robotStatus || "";
    robot.targetZone = robotState.targetZone || "";
    robot.targetLocationCode = targetLocationCode;
    robot.locationCode = targetLocationCode;
    robot.route = Array.isArray(robotState.route) ? robotState.route : [];
    robot.message = robotState.message || "";
    robot.waiting = Boolean(robotState.waiting);
    robot.blockedSegment = robotState.blockedSegment || "";
    robot.positionAnchor = resolveSegmentPositionAnchor(
      robot.currentPositionKey,
      robot.nextPositionKey,
      robot.segmentProgress,
      targetLocationCode
    );

    if (robot.positionAnchor && zoneData[robot.positionAnchor.zoneName]) {
      robot.currentZone = robot.positionAnchor.zoneName;
    }

    updateRobotControlState(robotKey);
    renderMissionFlowCard(robotKey);
  }

  function renderLiveMapState(state) {
    if (!state || !Array.isArray(state.robots)) {
      return;
    }

    state.robots.forEach(applyRobotState);

    if (window.WarehouseNotifications && typeof window.WarehouseNotifications.trackLiveMapState === "function") {
      window.WarehouseNotifications.trackLiveMapState(state);
    }

    if (selectedRobotKey && isBackendExecutionActive(robotDefinitions[selectedRobotKey]) && routeAnimating) {
      stopRouteAnimation();
    }

    if (selectedRobotKey) {
      currentZone = getRobotZone(selectedRobotKey);
      renderSelectedRobotStateMessage(selectedRobotKey);
    } else {
      if (missionFlowInstruction) {
        setLocalizedText(missionFlowInstruction, "Select a robot to view status, battery, strategy, and mission flow.");
        missionFlowInstruction.hidden = false;
      }
      setRouteAnimationMessage("");
    }

    renderCurrentMap();
  }

  function fetchLiveMapState() {
    if (liveMapStateRequestInFlight) {
      return;
    }

    liveMapStateRequestInFlight = true;

    fetch(LIVE_MAP_STATE_URL, {
      headers: {
        Accept: "application/json"
      }
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Live Map state request failed with HTTP " + response.status);
        }

        return response.json();
      })
      .then(renderLiveMapState)
      .catch(function (error) {
        console.warn("Live Map state polling failed. Keeping last known robot positions.", error);
      })
      .finally(function () {
        liveMapStateRequestInFlight = false;
      });
  }

  function startLiveMapPolling() {
    fetchLiveMapState();
    liveMapStatePollTimer = window.setInterval(fetchLiveMapState, LIVE_MAP_STATE_POLL_INTERVAL_MS);
  }

  function stopLiveMapPolling() {
    if (liveMapStatePollTimer) {
      window.clearInterval(liveMapStatePollTimer);
      liveMapStatePollTimer = null;
    }
  }

  function createRouteSvg(zoneName, showBridge, showStationConnectors) {
    var svg = createSvgElement("svg", {
      class: "zone-route-svg",
      viewBox: "0 0 900 430",
      "aria-hidden": "true",
      focusable: "false"
    });
    var lineGroup = createSvgElement("g", { class: "route-lines" });
    var nodeGroup = createSvgElement("g", { class: "route-nodes" });

    routeLinePaths.forEach(function (pathData) {
      lineGroup.appendChild(createSvgElement("path", {
        class: "route-line",
        d: pathData
      }));
    });

    svg.appendChild(lineGroup);
    svg.appendChild(createCargoApproachRoutes(zoneName));

    if (showBridge) {
      svg.appendChild(createBridgeRoutes(zoneName));
    }

    routeNodes.forEach(function (node) {
      var routePointName = getRoutePointNameForNode(zoneName, node.key);

      nodeGroup.appendChild(createSvgElement("circle", {
        class: "route-node-svg",
        "data-route-point": routePointName,
        "data-route-node": zoneName + "-" + node.key,
        cx: node.x,
        cy: node.y,
        r: 8
      }));
    });

    svg.appendChild(nodeGroup);

    if (showStationConnectors) {
      svg.appendChild(createStationConnectors(zoneName));
    }

    return svg;
  }

  function createCargoApproachRoutes(zoneName) {
    var approachGroup = createSvgElement("g", { class: "cargo-approach-routes" });
    var zoneLocations = locationCoordinates[zoneName] || {};

    Object.keys(zoneLocations).forEach(function (locationCode) {
      var slotPosition = zoneLocations[locationCode];
      var routePosition = getTargetRowRouteLocalPosition(locationCode);
      var approachPosition = getTargetApproachLocalPosition(slotPosition);
      var approachRoutePoint = locationCode + "-approach";
      var pathData;

      if (!routePosition || !approachPosition || !slotPosition) {
        return;
      }

      pathData = "M" + toSvgX(routePosition) + " " + toSvgY(routePosition)
        + " V" + toSvgY(approachPosition)
        + " H" + toSvgX(slotPosition);

      approachGroup.appendChild(createSvgElement("path", {
        class: "cargo-approach-line",
        "data-route-point": approachRoutePoint,
        "data-location": locationCode,
        d: pathData
      }));

      approachGroup.appendChild(createSvgElement("circle", {
        class: "cargo-approach-node",
        "data-route-point": approachRoutePoint,
        "data-location": locationCode,
        cx: toSvgX(approachPosition),
        cy: toSvgY(approachPosition),
        r: 5
      }));
    });

    return approachGroup;
  }

  function createBridgeRoutes(zoneName) {
    var bridgeGroup = createSvgElement("g", { class: "route-bridge-svg" });
    var bridgeRoutePointName = getBridgeRoutePointName(zoneName);

    [330, 570].forEach(function (x) {
      bridgeGroup.appendChild(createSvgElement("path", {
        class: "bridge-line-svg",
        d: "M" + x + " 28 V95"
      }));

      var attributes = {
        class: "bridge-node-svg",
        cx: x,
        cy: 58,
        r: 7
      };

      if (x === 330 && bridgeRoutePointName) {
        attributes["data-route-point"] = bridgeRoutePointName;
      }

      bridgeGroup.appendChild(createSvgElement("circle", attributes));
    });

    return bridgeGroup;
  }

  function createStationConnectors(zoneName) {
    var connectorGroup = createSvgElement("g", { class: "station-connectors" });
    var connectors = [
      { className: "connector-base", x: 330, routePointName: zoneName === "c" ? "base-station" : "" },
      { className: "connector-charge", x: 570 }
    ];

    connectors.forEach(function (connector) {
      var marker = createSvgElement("g", {
        class: "station-connector " + connector.className
      });

      marker.appendChild(createSvgElement("line", {
        class: "connector-line",
        x1: connector.x,
        y1: 344,
        x2: connector.x,
        y2: 385
      }));

      var dotAttributes = {
        class: "connector-dot",
        cx: connector.x,
        cy: 392,
        r: 8
      };

      if (connector.routePointName) {
        dotAttributes["data-route-point"] = connector.routePointName;
      }

      marker.appendChild(createSvgElement("circle", dotAttributes));

      connectorGroup.appendChild(marker);
    });

    return connectorGroup;
  }

  function createPackagePad(cargoClass, locationCode, position) {
    var pad = document.createElement("div");
    var cargo = document.createElement("span");

    pad.className = "package-pad";
    pad.id = "slot-" + locationCode.toLowerCase();
    pad.dataset.location = locationCode;
    pad.dataset.locationCode = locationCode;
    pad.style.setProperty("--pkg-left", position.left + "%");
    pad.style.setProperty("--pkg-top", position.top + "%");
    pad.setAttribute("aria-label", "Package slot " + locationCode);

    cargo.className = "cargo-dot " + cargoClass;
    cargo.setAttribute("aria-hidden", "true");
    pad.appendChild(cargo);

    return pad;
  }

  function renderPackageLayer(cargoClass, zoneName, zoneTitle) {
    var layer = document.createElement("div");
    var zoneLocations = locationCoordinates[zoneName] || {};

    layer.className = "zone-package-layer";
    layer.setAttribute("aria-label", zoneTitle + " package pads");

    Object.keys(zoneLocations).forEach(function (locationCode) {
      layer.appendChild(createPackagePad(cargoClass, locationCode, zoneLocations[locationCode]));
    });

    return layer;
  }

  function toWorldLineLengthPercent(startPosition, targetPosition) {
    var dx = (targetPosition.left - startPosition.left) / 100 * routeWorldWidth;
    var dy = (targetPosition.top - startPosition.top) / 100 * routeWorldHeight;

    return Math.sqrt(dx * dx + dy * dy) / routeWorldWidth * 100;
  }

  function toWorldLineAngle(startPosition, targetPosition) {
    var dx = (targetPosition.left - startPosition.left) / 100 * routeWorldWidth;
    var dy = (targetPosition.top - startPosition.top) / 100 * routeWorldHeight;

    return Math.atan2(dy, dx) * 180 / Math.PI;
  }

  function createRouteWorldSegment(startPosition, targetPosition, className, stepKey) {
    var line = document.createElement("div");
    var length;

    if (!startPosition || !targetPosition) {
      return null;
    }

    length = toWorldLineLengthPercent(startPosition, targetPosition);

    if (length < 0.1) {
      return null;
    }

    line.className = className + (stepKey === "return" ? " is-return" : "");
    line.style.setProperty("--route-left", startPosition.left + "%");
    line.style.setProperty("--route-top", startPosition.top + "%");
    line.style.setProperty("--route-length", length + "%");
    line.style.setProperty("--route-angle", toWorldLineAngle(startPosition, targetPosition) + "deg");

    return line;
  }

  function createRouteWorldZone(zoneName, highlighted) {
    var data = zoneData[zoneName];
    var layout = routeWorldZoneLayout[zoneName];
    var zone = document.createElement("article");
    var label = document.createElement("div");
    var title = document.createElement("strong");
    var subtitle = document.createElement("span");

    zone.className = "route-world-zone " + data.themeClass;
    zone.classList.toggle("is-highlighted", highlighted);
    zone.dataset.zone = zoneName;
    zone.style.setProperty("--world-zone-left", (layout.left / routeWorldWidth * 100) + "%");
    zone.style.setProperty("--world-zone-top", (layout.top / routeWorldHeight * 100) + "%");
    zone.style.setProperty("--world-zone-width", (layout.width / routeWorldWidth * 100) + "%");
    zone.style.setProperty("--world-zone-height", (layout.height / routeWorldHeight * 100) + "%");

    label.className = "route-world-zone-label";
    setLocalizedText(title, data.title);
    setLocalizedText(subtitle, data.subtitle);
    label.appendChild(title);
    label.appendChild(subtitle);
    zone.appendChild(label);

    return zone;
  }

  function createRouteWorldDot(routePointName, position) {
    var dot = document.createElement("span");

    dot.className = "route-world-dot";
    dot.classList.toggle("is-base", routePointName === "base-station");
    dot.classList.toggle("is-bridge", routePointName.indexOf("bridge-") === 0);
    dot.classList.toggle("is-approach", routePointName.indexOf("-approach") !== -1);
    dot.dataset.routePoint = routePointName;
    dot.style.setProperty("--world-left", position.left + "%");
    dot.style.setProperty("--world-top", position.top + "%");
    dot.setAttribute("aria-hidden", "true");

    return dot;
  }

  function createRouteWorldPackage(zoneName, cargoClass, locationCode, position) {
    var pad = document.createElement("div");
    var cargo = document.createElement("span");

    pad.className = "package-pad route-world-package";
    pad.dataset.location = locationCode;
    pad.dataset.locationCode = locationCode;
    pad.dataset.zone = zoneName;
    pad.style.setProperty("--pkg-left", position.left + "%");
    pad.style.setProperty("--pkg-top", position.top + "%");
    pad.setAttribute("aria-label", "Package slot " + locationCode);

    cargo.className = "cargo-dot " + cargoClass;
    cargo.setAttribute("aria-hidden", "true");
    pad.appendChild(cargo);

    return pad;
  }

  function createRouteWorldLabel(text, position, className) {
    var label = document.createElement("span");

    label.className = "route-world-point-label " + className;
    setLocalizedText(label, text);
    label.style.setProperty("--world-left", position.left + "%");
    label.style.setProperty("--world-top", position.top + "%");

    return label;
  }

  function createRouteWorldRoutesLayer() {
    var layer = document.createElement("div");
    var baseOutboundSegment = createRouteWorldSegment(
      routeWaypoints["base-station"].position,
      routeWaypoints["zone-c-left-entry"].position,
      "route-world-line is-station-line",
      "move"
    );
    var baseReturnSegment = createRouteWorldSegment(
      routeWaypoints["zone-c-right-exit"].position,
      routeWaypoints["base-station"].position,
      "route-world-line is-station-line",
      "return"
    );

    layer.className = "route-world-route-layer";

    ["a", "b", "c"].forEach(function (zoneName) {
      routeGridSegmentKeys.forEach(function (segment) {
        var start = routeWaypoints[getRoutePointNameForNode(zoneName, segment[0])];
        var target = routeWaypoints[getRoutePointNameForNode(zoneName, segment[1])];
        var line = createRouteWorldSegment(start.position, target.position, "route-world-line", "move");

        if (line) {
          layer.appendChild(line);
        }
      });
    });

    [
      ["zone-c-left-main-2", "bridge-c-b-left-1"],
      ["bridge-c-b-left-1", "bridge-c-b-left-2"],
      ["bridge-c-b-left-2", "zone-b-left-entry"],
      ["zone-b-left-main-2", "bridge-b-a-left-1"],
      ["bridge-b-a-left-1", "bridge-b-a-left-2"],
      ["bridge-b-a-left-2", "zone-a-left-entry"],
      ["zone-a-right-exit", "bridge-a-b-right-1"],
      ["bridge-a-b-right-1", "bridge-a-b-right-2"],
      ["bridge-a-b-right-2", "zone-b-right-main-2"],
      ["zone-b-right-exit", "bridge-b-c-right-1"],
      ["bridge-b-c-right-1", "bridge-b-c-right-2"],
      ["bridge-b-c-right-2", "zone-c-right-main-2"]
    ].forEach(function (segment) {
      var start = routeWaypoints[segment[0]];
      var target = routeWaypoints[segment[1]];
      var line = createRouteWorldSegment(start.position, target.position, "route-world-line is-bridge-line", "move");

      if (line) {
        layer.appendChild(line);
      }
    });

    appendRouteWorldApproachLines(layer);

    if (baseOutboundSegment) {
      layer.appendChild(baseOutboundSegment);
    }

    if (baseReturnSegment) {
      layer.appendChild(baseReturnSegment);
    }

    return layer;
  }

  function appendRouteWorldApproachLines(layer) {
    ["a", "b", "c"].forEach(function (zoneName) {
      var zoneLocations = locationCoordinates[zoneName] || {};

      Object.keys(zoneLocations).forEach(function (locationCode) {
        var routeStart = toRouteWorldPosition(zoneName, getTargetRowRouteLocalPosition(locationCode));
        var approach = routeWaypoints[locationCode + "-approach"];
        var slot = toRouteWorldPosition(zoneName, zoneLocations[locationCode]);

        [
          [routeStart, approach ? approach.position : null],
          [approach ? approach.position : null, slot]
        ].forEach(function (segment) {
          var line = createRouteWorldSegment(segment[0], segment[1], "route-world-line is-approach-line", "move");

          if (line) {
            layer.appendChild(line);
          }
        });
      });
    });
  }

  function createRouteWorldDotsLayer() {
    var layer = document.createElement("div");

    layer.className = "route-world-dot-layer";

    Object.keys(routeWaypoints).forEach(function (routePointName) {
      layer.appendChild(createRouteWorldDot(routePointName, routeWaypoints[routePointName].position));
    });

    layer.appendChild(createRouteWorldLabel("Base Station", routeWaypoints["base-station"].position, "is-base-label"));

    return layer;
  }

  function createRouteWorldPackagesLayer() {
    var layer = document.createElement("div");

    layer.className = "route-world-package-layer";

    ["a", "b", "c"].forEach(function (zoneName) {
      var data = zoneData[zoneName];
      var zoneLocations = locationCoordinates[zoneName] || {};

      Object.keys(zoneLocations).forEach(function (locationCode) {
        layer.appendChild(createRouteWorldPackage(
          zoneName,
          data.cargoClass,
          locationCode,
          toRouteWorldPosition(zoneName, zoneLocations[locationCode])
        ));
      });
    });

    return layer;
  }

  function createRouteAnimationRobotMarker(robotKey, startPosition) {
    var robot = robotDefinitions[robotKey];
    var marker = document.createElement("button");
    var robotShape = document.createElement("span");
    var label = document.createElement("span");

    marker.type = "button";
    marker.className = "map-robot-marker route-animation-robot " + robot.className + " is-selected is-route-animating";
    marker.dataset.robotMarker = robotKey;
    marker.dataset.currentZone = "route";
    marker.dataset.locationCode = robot.locationCode;
    marker.setAttribute("aria-label", robot.label + " route animation");
    setMarkerPosition(marker, startPosition);

    robotShape.className = "robot-symbol " + robot.className;
    robotShape.setAttribute("aria-hidden", "true");
    label.className = "robot-marker-label";
    label.textContent = robot.label;

    marker.appendChild(robotShape);
    marker.appendChild(label);
    marker.addEventListener("click", function () {
      selectRobot(robotKey);
    });

    return marker;
  }

  function renderRouteAnimationMap(robotKey, targetZoneName, route) {
    var routeMap = document.createElement("section");

    activeZoneMap.classList.remove("is-overview");
    activeZoneMap.classList.add("is-route-animation");

    routeMap.className = "route-animation-map";
    routeMap.dataset.routeAnimationMap = "true";
    routeMap.setAttribute("aria-label", "Full warehouse route animation map");

    ["a", "b", "c"].forEach(function (zoneName) {
      routeMap.appendChild(createRouteWorldZone(zoneName, zoneName === targetZoneName));
    });

    routeMap.appendChild(createRouteWorldRoutesLayer());
    routeMap.appendChild(createRouteWorldPackagesLayer());
    routeMap.appendChild(createRouteWorldDotsLayer());
    routeMap.appendChild(createRouteAnimationRobotMarker(robotKey, route[0].position));

    activeZoneMap.replaceChildren(routeMap);
    drawRoutePreviewPath(routeMap, route);
    translateFragment(routeMap);

    return routeMap;
  }

  function removeRoutePreviewLine() {
    activeZoneMap.querySelectorAll(".route-preview-line").forEach(function (line) {
      line.remove();
    });
  }

  function drawRoutePreviewPath(canvas, route) {
    route.forEach(function (waypoint, index) {
      var nextWaypoint = route[index + 1];
      var line;

      if (!nextWaypoint) {
        return;
      }

      line = createRouteWorldSegment(waypoint.position, nextWaypoint.position, "route-preview-line", nextWaypoint.stepKey);

      if (!line) {
        return;
      }

      canvas.appendChild(line);
    });
  }

  function setMarkerPosition(marker, position) {
    marker.style.left = position.left + "%";
    marker.style.top = position.top + "%";
  }

  function resetRouteAnimationMarkers() {
    activeZoneMap.querySelectorAll(".map-robot-marker.is-route-animating").forEach(function (marker) {
      marker.classList.remove("is-route-animating");
      marker.style.transition = "";
    });
  }

  function clearRouteAnimationState() {
    animationRobotKey = null;
  }

  function createRobotMarker(robotKey, zoneName) {
    var robot = robotDefinitions[robotKey];
    var robotZone = getRobotZone(robotKey);
    var targetPosition;
    var visualPosition;
    var marker = document.createElement("button");
    var robotShape = document.createElement("span");
    var label = document.createElement("span");
    var isSelected = robotKey === selectedRobotKey;

    if (robotZone !== zoneName) {
      return null;
    }

    if (selectedRobotKey && !isSelected) {
      return null;
    }

    targetPosition = getRobotPosition(robotKey, zoneName);
    visualPosition = getRobotVisualRenderPosition(robotKey, zoneName, targetPosition);

    marker.type = "button";
    marker.className = "map-robot-marker " + robot.className;
    marker.classList.toggle("is-selected", isSelected);
    marker.classList.toggle("is-backend-live", isBackendExecutionActive(robot));
    marker.dataset.robotMarker = robotKey;
    marker.dataset.currentZone = robotZone;
    marker.dataset.locationCode = robot.locationCode;
    marker.dataset.currentPositionKey = robot.currentPositionKey || "";
    marker.dataset.nextPositionKey = robot.nextPositionKey || "";
    marker.dataset.segmentProgress = String(robot.segmentProgress || 0);
    marker.dataset.positionAnchor = robot.positionAnchor ? robot.positionAnchor.anchorKey : "";
    marker.dataset.positionAnchorType = robot.positionAnchor ? robot.positionAnchor.anchorType : "";
    marker.style.setProperty("--robot-motion-duration", getRobotVisualMovementDurationMs(robotKey) + "ms");
    marker.style.setProperty("--robot-motion-easing", ROBOT_VISUAL_MOVEMENT_EASING);
    marker.dataset.targetLeft = targetPosition.left + "%";
    marker.dataset.targetTop = targetPosition.top + "%";
    applyMarkerCssPosition(marker, visualPosition || targetPosition);
    marker.setAttribute("aria-label", "Focus " + robot.label + (robot.currentPositionKey ? " at " + robot.currentPositionKey : ""));

    robotShape.className = "robot-symbol " + robot.className;
    robotShape.setAttribute("aria-hidden", "true");
    label.className = "robot-marker-label";
    label.textContent = robot.label;

    marker.appendChild(robotShape);
    marker.appendChild(label);
    marker.addEventListener("click", function () {
      selectRobot(robotKey);
    });

    return marker;
  }

  function renderRobotLayer(zoneName) {
    var layer = document.createElement("div");

    layer.className = "robot-layer";
    layer.setAttribute("aria-label", "Warehouse robot markers");

    Object.keys(robotDefinitions).forEach(function (robotKey) {
      var marker = createRobotMarker(robotKey, zoneName);

      if (marker) {
        layer.appendChild(marker);
      }
    });

    return layer;
  }

  function createZoneArticle(zoneName, highlighted) {
    var data = zoneData[zoneName];
    var zone = document.createElement("article");
    var heading = document.createElement("div");
    var titleGroup = document.createElement("div");
    var title = document.createElement("h2");
    var subtitle = document.createElement("p");
    var count = document.createElement("span");
    var canvas = document.createElement("div");

    zone.className = "warehouse-zone " + data.themeClass;
    zone.classList.toggle("is-highlighted", highlighted);
    zone.dataset.zone = zoneName;
    zone.setAttribute("aria-labelledby", "active-zone-title-" + zoneName);

    heading.className = "zone-heading";
    title.id = "active-zone-title-" + zoneName;
    setLocalizedText(title, data.title);
    setLocalizedText(subtitle, data.subtitle);
    count.className = "zone-count";
    setLocalizedText(count, "9 PKG");

    titleGroup.appendChild(title);
    titleGroup.appendChild(subtitle);
    heading.appendChild(titleGroup);
    heading.appendChild(count);

    canvas.className = "zone-map-canvas";
    canvas.appendChild(createRouteSvg(zoneName, data.showBridge, data.showStationConnectors));
    canvas.appendChild(renderPackageLayer(data.cargoClass, zoneName, data.title));
    canvas.appendChild(renderRobotLayer(zoneName));

    zone.appendChild(heading);
    zone.appendChild(canvas);
    return zone;
  }

  function renderActiveZone(zoneName) {
    activeZoneMap.classList.remove("is-overview");
    activeZoneMap.classList.remove("is-route-animation");
    activeZoneMap.replaceChildren(createZoneArticle(zoneName, true));
  }

  function renderWarehouseOverview() {
    var overview = document.createElement("section");

    overview.className = "warehouse-overview";
    overview.setAttribute("aria-label", "All warehouse zones with current robot positions");

    Object.keys(zoneData).forEach(function (zoneName) {
      overview.appendChild(createZoneArticle(zoneName, zoneName === currentZone));
    });

    activeZoneMap.classList.remove("is-route-animation");
    activeZoneMap.classList.add("is-overview");
    activeZoneMap.replaceChildren(overview);
  }

  function updateStationVisibility(zoneName) {
    if (!stationRow) {
      return;
    }

    var shouldShowStations = zoneName === "c";

    stationRow.hidden = !shouldShowStations;
    stationRow.classList.toggle("is-hidden", !shouldShowStations);
  }

  function buildStatusText(zoneName) {
    var zone = zoneData[zoneName] || zoneData.a;

    if (!selectedRobotKey) {
      return "Showing all robots across all zones";
    }

    var robot = robotDefinitions[selectedRobotKey] || robotDefinitions.picker;
    if (routeAnimating && animationRobotKey === selectedRobotKey) {
      return "Animating " + robot.label + " from Base Station through warehouse route dots";
    }

    return "Focused on " + robot.label + " in " + zone.label;
  }

  function renderCurrentMap() {
    var renderedZone = selectedRobotKey ? getRobotZone(selectedRobotKey) : currentZone;

    if (selectedRobotKey) {
      renderActiveZone(renderedZone);
    } else {
      renderWarehouseOverview();
    }

    updateStationVisibility(renderedZone);

    if (zoneStatus) {
      setLocalizedText(zoneStatus, buildStatusText(renderedZone));
    }

    zoneButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.zoneTarget === renderedZone);
    });

    updateRouteAnimationButton();
    translateFragment(activeZoneMap);
    translateFragment(stationRow);
    scheduleRobotMarkerAnimations();
  }

  function updateMissionFlowStep(robotKey, stepKey) {
    robotFlowCards.forEach(function (card) {
      if (card.dataset.robotFlow !== robotKey) {
        return;
      }

      card.querySelectorAll("[data-flow-step]").forEach(function (step) {
        step.classList.toggle("is-current", step.dataset.flowStep === stepKey);
      });
    });
  }

  function resetMissionFlowStep(robotKey) {
    var card = document.querySelector("[data-robot-flow='" + robotKey + "']");
    var robot = robotDefinitions[robotKey];

    if (!card) {
      return;
    }

    updateMissionFlowStep(
      robotKey,
      robot && robot.hasActiveMission
        ? executionStepToFlowStep(robot.executionStep)
        : card.dataset.activeStep || "none"
    );
  }

  function clearAnimationTimers() {
    if (animationTimer) {
      window.clearTimeout(animationTimer);
      animationTimer = null;
    }

    if (pickupHighlightTimer) {
      window.clearTimeout(pickupHighlightTimer);
      pickupHighlightTimer = null;
    }
  }

  function stopRouteAnimation() {
    clearAnimationTimers();
    routeAnimating = false;
    removeRoutePreviewLine();
    resetRouteAnimationMarkers();
    clearRouteAnimationState();
    updateRouteAnimationButton();
  }

  function finishRouteAnimation(robotKey, marker) {
    routeAnimating = false;
    updateMissionFlowStep(robotKey, "returned");
    setRouteAnimationMessage("Returned to Base. Waiting for confirmation.");

    if (marker) {
      marker.classList.remove("is-route-animating");
      marker.style.transition = "";
    }

    clearRouteAnimationState();
    updateRouteAnimationButton();
  }

  function setActiveZone(zoneName) {
    var data = zoneData[zoneName];

    if (!data) {
      return;
    }

    if (routeAnimating) {
      stopRouteAnimation();
    }

    currentZone = zoneName;
    renderCurrentMap();

    routeIndex = demoRoute.indexOf(zoneName);
    if (routeIndex < 0) {
      routeIndex = 0;
    }
  }

  function selectRobot(robotKey) {
    if (!robotDefinitions[robotKey]) {
      return;
    }

    stopRouteAnimation();
    setRouteAnimationMessage("");
    selectedRobotKey = robotKey;
    currentZone = getRobotZone(robotKey);

    robotControlButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.robotSelect === robotKey);
    });

    if (showAllRobotsButton) {
      showAllRobotsButton.classList.remove("is-active");
    }

    if (missionFlowInstruction) {
      missionFlowInstruction.hidden = true;
    }

    if (mapDisplayMode) {
      setLocalizedText(mapDisplayMode, "Selected Robot View");
    }

    robotFlowCards.forEach(function (card) {
      var isActive = card.dataset.robotFlow === robotKey;
      card.classList.toggle("is-active", isActive);
      card.hidden = !isActive;
    });

    resetMissionFlowStep(robotKey);

    renderSelectedRobotStateMessage(robotKey);

    renderCurrentMap();
  }

  function showAllRobots() {
    stopRouteAnimation();
    setRouteAnimationMessage("");
    selectedRobotKey = null;

    robotControlButtons.forEach(function (button) {
      button.classList.remove("is-active");
    });

    robotFlowCards.forEach(function (card) {
      card.classList.remove("is-active");
      card.hidden = true;
    });

    if (missionFlowInstruction) {
      setLocalizedText(missionFlowInstruction, "Select a robot to view status, battery, strategy, and mission flow.");
      missionFlowInstruction.hidden = false;
    }

    if (showAllRobotsButton) {
      showAllRobotsButton.classList.add("is-active");
    }

    if (mapDisplayMode) {
      setLocalizedText(mapDisplayMode, "All Robots");
    }

    renderCurrentMap();
  }

  function showNextZone() {
    routeIndex = (routeIndex + 1) % demoRoute.length;
    setActiveZone(demoRoute[routeIndex]);
  }

  function startRouteAnimation() {
    var robotKey = selectedRobotKey;
    var zoneName = getRobotZone(robotKey);
    var robot = robotDefinitions[robotKey];
    var route;
    var marker;
    var waypointIndex = 1;
    var routeMap;

    if (!robotKey || !robot || !hasRobotMissionTarget(robotKey)) {
      setRouteAnimationMessage("Select a robot with an active pickup mission before starting visual preview.");
      updateRouteAnimationButton();
      return;
    }

    if (isBackendExecutionActive(robot)) {
      setRouteAnimationMessage("Live Map is following backend execution state.");
      updateRouteAnimationButton();
      return;
    }

    zoneName = getZoneFromLocationCode(robot.locationCode);
    stopRouteAnimation();
    selectedRobotKey = robotKey;
    currentZone = zoneName;
    routeAnimating = true;
    animationRobotKey = robotKey;
    route = buildPickupRoute(robotKey, zoneName);
    setRouteAnimationMessage("Preview only. Backend state is unchanged.");
    updateMissionFlowStep(robotKey, "move");
    routeMap = renderRouteAnimationMap(robotKey, zoneName, route);
    marker = routeMap.querySelector("[data-robot-marker='" + robotKey + "']");

    if (!marker) {
      routeAnimating = false;
      clearRouteAnimationState();
      setRouteAnimationMessage("Route preview could not start because the selected robot marker is not visible.");
      updateRouteAnimationButton();
      return;
    }

    marker.classList.add("is-route-animating");
    marker.style.transition = "none";
    setMarkerPosition(marker, route[0].position);
    marker.offsetHeight;

    function animateNextWaypoint() {
      var waypoint = route[waypointIndex];
      var movingStep;

      if (!routeAnimating || !waypoint) {
        return;
      }

      movingStep = waypoint.stepKey === "pickup" ? "move" : waypoint.stepKey;
      updateMissionFlowStep(robotKey, movingStep);

      if (movingStep === "return") {
        setRouteAnimationMessage("Preview only: returning to Base.");
      } else {
        setRouteAnimationMessage("Preview only: moving to " + robot.locationCode + ".");
      }

      marker.style.transition = "top " + routeSegmentDuration
        + "ms ease-in-out, left " + routeSegmentDuration
        + "ms ease-in-out, transform 160ms ease";
      setMarkerPosition(marker, waypoint.position);

      animationTimer = window.setTimeout(function () {
        animationTimer = null;

        if (!routeAnimating) {
          return;
        }

        if (waypoint.stepKey === "pickup") {
          updateMissionFlowStep(robotKey, "pickup");
          setRouteAnimationMessage("Preview only: pickup at " + robot.locationCode + ".");
          pickupHighlightTimer = window.setTimeout(function () {
            pickupHighlightTimer = null;
            waypointIndex += 1;
            animateNextWaypoint();
          }, pickupPauseDuration);
          return;
        }

        if (waypointIndex >= route.length - 1) {
          finishRouteAnimation(robotKey, marker);
          return;
        }

        waypointIndex += 1;
        animateNextWaypoint();
      }, routeSegmentDuration);
    }

    animationTimer = window.setTimeout(animateNextWaypoint, 80);
  }

  zoneButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setActiveZone(button.dataset.zoneTarget);
    });
  });

  robotControlButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      selectRobot(button.dataset.robotSelect);
    });
  });

  if (showAllRobotsButton) {
    showAllRobotsButton.addEventListener("click", showAllRobots);
  }

  if (nextZoneButton) {
    nextZoneButton.addEventListener("click", showNextZone);
  }

  if (startRouteButton) {
    startRouteButton.addEventListener("click", startRouteAnimation);
  }

  window.setActiveZone = setActiveZone;
  window.selectLiveMapRobot = selectRobot;
  window.showAllLiveMapRobots = showAllRobots;
  window.startLiveMapRouteAnimation = startRouteAnimation;
  window.fetchLiveMapState = fetchLiveMapState;
  window.addEventListener("beforeunload", stopLiveMapPolling);
  hydrateRobotMissionData();
  showAllRobots();
  startLiveMapPolling();
});
