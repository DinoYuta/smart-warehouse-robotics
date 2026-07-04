document.addEventListener("DOMContentLoaded", function () {
  var activeZoneMap = document.getElementById("active-zone-map");
  var stationRow = document.getElementById("station-row");
  var zoneStatus = document.getElementById("zone-status");
  var zoneButtons = document.querySelectorAll("[data-zone-target]");
  var missionSteps = document.querySelectorAll(".flow-step");
  var nextZoneButton = document.getElementById("next-zone-button");
  var startRouteButton = document.getElementById("start-route-button");
  var demoRoute = ["c", "b", "a"];
  var routeIndex = 0;
  var routeTimer = null;
  var svgNamespace = "http://www.w3.org/2000/svg";

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
    { x: 160, y: 95 },
    { x: 330, y: 95 },
    { x: 570, y: 95 },
    { x: 740, y: 95 },
    { x: 160, y: 215 },
    { x: 330, y: 215 },
    { x: 570, y: 215 },
    { x: 740, y: 215 },
    { x: 160, y: 335 },
    { x: 330, y: 335 },
    { x: 570, y: 335 },
    { x: 740, y: 335 }
  ];

  var packagePositions = [
    { x: 245, y: 72 },
    { x: 450, y: 72 },
    { x: 655, y: 72 },
    { x: 245, y: 192 },
    { x: 450, y: 192 },
    { x: 655, y: 192 },
    { x: 245, y: 312 },
    { x: 450, y: 312 },
    { x: 655, y: 312 }
  ];

  var zoneData = {
    a: {
      title: "ZONE A",
      subtitle: "SMALL CARGO",
      cargoClass: "cargo-small",
      themeClass: "zone-a",
      showBridge: false,
      showStationConnectors: false,
      statusText: "Robot currently viewing Zone A"
    },
    b: {
      title: "ZONE B",
      subtitle: "MEDIUM CARGO",
      cargoClass: "cargo-medium",
      themeClass: "zone-b",
      showBridge: true,
      showStationConnectors: false,
      statusText: "Robot currently viewing Zone B"
    },
    c: {
      title: "ZONE C",
      subtitle: "LARGE CARGO",
      cargoClass: "cargo-large",
      themeClass: "zone-c",
      showBridge: true,
      showStationConnectors: true,
      statusText: "Robot currently viewing Zone C"
    }
  };

  function createSvgElement(tagName, attributes) {
    var element = document.createElementNS(svgNamespace, tagName);

    Object.keys(attributes).forEach(function (name) {
      element.setAttribute(name, attributes[name]);
    });

    return element;
  }

  function createRouteSvg(showBridge, showStationConnectors) {
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

    if (showBridge) {
      svg.appendChild(createBridgeRoutes());
    }

    routeNodes.forEach(function (node) {
      nodeGroup.appendChild(createSvgElement("circle", {
        class: "route-node-svg",
        cx: node.x,
        cy: node.y,
        r: 7
      }));
    });

    svg.appendChild(nodeGroup);

    if (showStationConnectors) {
      svg.appendChild(createStationConnectors());
    }

    return svg;
  }

  function createBridgeRoutes() {
    var bridgeGroup = createSvgElement("g", { class: "route-bridge-svg" });

    [330, 570].forEach(function (x) {
      bridgeGroup.appendChild(createSvgElement("path", {
        class: "bridge-line-svg",
        d: "M" + x + " 28 V95"
      }));

      bridgeGroup.appendChild(createSvgElement("circle", {
        class: "bridge-node-svg",
        cx: x,
        cy: 58,
        r: 6
      }));
    });

    return bridgeGroup;
  }

  function createStationConnectors() {
    var connectorGroup = createSvgElement("g", { class: "station-connectors" });
    var connectors = [
      { className: "connector-base", x: 330 },
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

      marker.appendChild(createSvgElement("circle", {
        class: "connector-dot",
        cx: connector.x,
        cy: 392,
        r: 7
      }));

      connectorGroup.appendChild(marker);
    });

    return connectorGroup;
  }

  function createPackagePad(cargoClass, x, y, index) {
    var pad = document.createElement("div");
    var cargo = document.createElement("span");

    pad.className = "package-pad";
    pad.style.setProperty("--pkg-left", (x / 900 * 100) + "%");
    pad.style.setProperty("--pkg-top", (y / 430 * 100) + "%");
    pad.setAttribute("aria-label", "Package pad " + index);

    cargo.className = "cargo-dot " + cargoClass;
    cargo.setAttribute("aria-hidden", "true");
    pad.appendChild(cargo);

    return pad;
  }

  function renderPackageLayer(cargoClass, zoneTitle) {
    var layer = document.createElement("div");

    layer.className = "zone-package-layer";
    layer.setAttribute("aria-label", zoneTitle + " package pads");

    packagePositions.forEach(function (position, index) {
      layer.appendChild(createPackagePad(cargoClass, position.x, position.y, index + 1));
    });

    return layer;
  }

  function renderActiveZone(zoneName) {
    var data = zoneData[zoneName];
    var zone = document.createElement("article");
    var heading = document.createElement("div");
    var titleGroup = document.createElement("div");
    var title = document.createElement("h2");
    var subtitle = document.createElement("p");
    var count = document.createElement("span");
    var canvas = document.createElement("div");

    zone.className = "warehouse-zone " + data.themeClass + " is-highlighted";
    zone.dataset.zone = zoneName;
    zone.setAttribute("aria-labelledby", "active-zone-title");

    heading.className = "zone-heading";
    title.id = "active-zone-title";
    title.textContent = data.title;
    subtitle.textContent = data.subtitle;
    count.className = "zone-count";
    count.textContent = "9 PKG";

    titleGroup.appendChild(title);
    titleGroup.appendChild(subtitle);
    heading.appendChild(titleGroup);
    heading.appendChild(count);

    canvas.className = "zone-map-canvas";
    canvas.appendChild(createRouteSvg(data.showBridge, data.showStationConnectors));
    canvas.appendChild(renderPackageLayer(data.cargoClass, data.title));

    zone.appendChild(heading);
    zone.appendChild(canvas);
    activeZoneMap.replaceChildren(zone);
  }

  function updateStationVisibility(zoneName) {
    var shouldShowStations = zoneName === "c";

    stationRow.hidden = !shouldShowStations;
    stationRow.classList.toggle("is-hidden", !shouldShowStations);
  }

  function setMissionStep(stepIndex) {
    missionSteps.forEach(function (step, index) {
      step.classList.toggle("is-active", index === stepIndex);
    });
  }

  function setActiveZone(zoneName) {
    var data = zoneData[zoneName];

    if (!data) {
      return;
    }

    renderActiveZone(zoneName);
    updateStationVisibility(zoneName);
    zoneStatus.textContent = data.statusText;

    zoneButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.zoneTarget === zoneName);
    });

    routeIndex = demoRoute.indexOf(zoneName);
    if (routeIndex < 0) {
      routeIndex = 0;
    }

    setMissionStep(2);
  }

  function showNextZone() {
    routeIndex = (routeIndex + 1) % demoRoute.length;
    setActiveZone(demoRoute[routeIndex]);
  }

  function startDemoRoute() {
    var step = 0;

    if (routeTimer) {
      window.clearInterval(routeTimer);
    }

    setActiveZone(demoRoute[step]);
    routeTimer = window.setInterval(function () {
      step += 1;

      if (step >= demoRoute.length) {
        window.clearInterval(routeTimer);
        routeTimer = null;
        return;
      }

      setActiveZone(demoRoute[step]);
    }, 900);
  }

  zoneButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setActiveZone(button.dataset.zoneTarget);
    });
  });

  missionSteps.forEach(function (step, index) {
    step.addEventListener("click", function () {
      setMissionStep(index);
    });
  });

  nextZoneButton.addEventListener("click", showNextZone);
  startRouteButton.addEventListener("click", startDemoRoute);

  window.setActiveZone = setActiveZone;
  setActiveZone("a");
});
