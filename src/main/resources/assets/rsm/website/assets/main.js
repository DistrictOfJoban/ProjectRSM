L.CRS.Minecraft = L.Util.extend(L.CRS.Simple, {
    transformation: new L.Transformation(0.5, 0, 0.5, 0),
});

import { SETTINGS } from "./settings.js"
import { UTIL } from "./util.js"

let map = L.map("map", {
    crs: L.CRS.Minecraft,
    minZoom: 1,
    maxZoom: 6,
    renderer: L.canvas(),
    zoomControl: true,
}).setView([0, 0], 2);

map.createPane("stations");
map.createPane("tracks");
map.createPane("tracks-secondary");
map.createPane("occupy");
map.createPane("trains");
map.createPane("signals");
map.getPane("stations").style.zIndex = 400;
map.getPane("tracks").style.zIndex = 600;
map.getPane("tracks-secondary").style.zIndex = 650;
map.getPane("occupy").style.zIndex = 700;
map.getPane("occupy").style.pointerEvents = 'none';
map.getPane("trains").style.zIndex = 750;

map.getPane("tooltipPane").style.zIndex = 1000;

let areaLayer = L.layerGroup([], { pane: "stations" }).addTo(map);
let tracksLayer = L.layerGroup([], { pane: "tracks" }).addTo(map);
let tracksLayerSecondary = L.layerGroup([], { pane: "tracks-secondary" }).addTo(map);
let occupyLayer = L.layerGroup([], { pane: "occupy" }).addTo(map);
let trainLayer = L.layerGroup([], { pane: "trains" }).addTo(map);

let trackUpdated = false;

const knownTrains = new Map();
const knownOccupants = new Map();
let areaData = null;
let trainsStream = null;
let occupyStream = null;
let trackStream = null;

function updateAreaVisibility() {
    SETTINGS.STATION_VISIBLE = document.getElementById("stn-visible").checked;
    SETTINGS.DEPOT_VISIBLE = document.getElementById("depot-visible").checked;
    drawAreas();
}

document.getElementById("stn-visible").onchange = updateAreaVisibility;
document.getElementById("depot-visible").onchange = updateAreaVisibility;

function updateTracks() {
    if(trackStream) trackStream.close();
    trackStream = new EventSource(UTIL.formURL("data", map.getCenter().lng, map.getCenter().lat));

    trackStream.onmessage = (e) => {
        let data = e.data;
        let primaryLayer = trackUpdated ? tracksLayer : tracksLayerSecondary;
        let alternateLayer = trackUpdated ? tracksLayerSecondary : tracksLayer;

        if(data == 'READY2CLOSE') {
            alternateLayer.clearLayers();
            trackUpdated = !trackUpdated;
            trackStream.close();
            return;
        }

        let rail = JSON.parse(e.data);
        // Draw border if is platform or siding
        if(rail.type == "PLATFORM" || rail.type == "SIDING") {
            L.polyline([UTIL.xz(rail.pos1), UTIL.xz(rail.pos2)], {
                stroke: true,
                color: UTIL.convertColor(rail.color),
                weight: SETTINGS.LINE_THICKNESS + 4,
                lineCap: "square",
                pane: "tracks",
            })
            .addTo(primaryLayer);
        }

        // Actual Line
        L.polyline([UTIL.xz(rail.pos1), UTIL.xz(rail.pos2)], {
            color: "#aaa",
            weight: SETTINGS.LINE_THICKNESS,
            lineCap: "square",
            interactive: true,
            pane: "tracks"
        }).bindTooltip(
            `<div class="tooltip-wrapper" ${rail.type == "PLATFORM" || rail.type == "SIDING" ? `style="--bcolor: ${UTIL.convertColor(rail.color)}"` : ""}>
                <span class="header">${rail.type == "PLATFORM" ? `Platform ${rail.platNumber}` : "Rail"}</span>
                <br>Rail Type: ${rail.type}
                <br>Length: ${Math.round(rail.length)}m
                ${rail.type == "PLATFORM" ? `
                <br>Dwell time: ${rail.dwell}s
                ${rail.rtVia ? `<br>Route Via: ${UTIL.formatRouteVia(rail.rtVia)}` : ""}
                ` : ""}
            </div>`, {
            className: "tooltip",
            direction: "right",
            opacity: SETTINGS.TOOLTIP_OPACITY
            }
        ).addTo(primaryLayer);
    }
}

function renderOccupants() {
    occupyLayer.clearLayers();
    for(let section of knownOccupants.values()) {
        L.polyline([UTIL.xz(section.pos1), UTIL.xz(section.pos2)], {
            color: "#fcba03",
            weight: SETTINGS.LINE_THICKNESS,
            lineCap: "square",
            interactive: false,
            pane: "occupy",
        }).addTo(occupyLayer);
    }
}

function renderTrains() {
    let center = map.getCenter();

    trainLayer.clearLayers();
    let outdatedTrains = [];
    knownTrains.forEach((train, uuid) => {
        let prevPos = null;

        if(train.speed > 0 && Date.now() - train.lastUpdated > 1000 * 20) {
            outdatedTrains.push(uuid);
            return;
        }
        
        train.poses.forEach((pos, i) => {
            // if(!prevPos) {
            //     prevPos = pos;
            //     return;
            // }

            if(UTIL.manhattenDistance(center.lng, center.lat, pos[0], pos[1]) > 2000) {
                return;
            }

            let speed = Math.round(train.speed * 10) / 10;
            /* Draw white line */
            L.polyline([UTIL.xz(pos[0]), UTIL.xz(pos[1])], {
                color: "#FFF",
                weight: SETTINGS.LINE_THICKNESS,
                lineCap: "square",
                className: "train",
                pane: "trains"
            }).addTo(trainLayer);

            /* Draw invisible line to give a little margin for the tooltip hover */
            L.polyline([UTIL.xz(pos[0]), UTIL.xz(pos[1])], {
                    color: "transparent",
                    weight: SETTINGS.LINE_THICKNESS + 4,
                    lineCap: "square",
                    className: "train",
                    pane: "trains"
            })
            .bindTooltip(
                `<div class="tooltip-wrapper" style="--bcolor: ${UTIL.convertColor(train.color)}">
                <span class="header">${train.id} <span class="train-tag">${train.cars}-cars</span></span>
                ${train.routeName ? `<br>Route: ${train.routeName}` : ""}
                <br>Started up: <b>${train.started}</b>
                ${train.dest ? `<br>To: <b>${train.dest}</b>` : ""}
                <br>Mode: <b>${train.isManual ? "Manual" : "ATO"}</b>
                <br>Speed: <b>${speed} km/h</b>
                ${train.passengers ? `<br>Passengers: <b>${train.passengers}</b>
                </div>` : ""}`,
                {
                    className: "tooltip",
                    direction: "right",
                    offset: L.point(12, 0),
                    opacity: SETTINGS.TOOLTIP_OPACITY
                }
            )
            .addTo(trainLayer);
        
            // prevPos = pos;
        });
    });

    for(let outdatedTrain of outdatedTrains) {
        knownTrains.delete(outdatedTrain);
    }
}

async function getAreas() {
    let resp = await fetch(SETTINGS.BASE_URL + "areas");
    if(!resp.ok) return;
    areaData = await resp.json();
}

async function drawAreas() {
    areaLayer.clearLayers();

    if(SETTINGS.STATION_VISIBLE) {
        areaData.station.forEach((stn) => {
            L.rectangle([UTIL.xz(stn.corner1), UTIL.xz(stn.corner2)], {
                color: UTIL.convertColor(stn.color),
                opacity: 0.2
            }).addTo(areaLayer)
        
            // Draw text
            new L.marker(UTIL.getCenter(stn.corner1, stn.corner2), { opacity: 0 })
            .bindTooltip(`${stn.name}`, {
                permanent: true,
                direction: 'center',
                className: "stn-label"
            })
            .addTo(areaLayer);
        });
    }

    if(SETTINGS.DEPOT_VISIBLE) {
        areaData.depots.forEach((stn) => {
            L.rectangle([UTIL.xz(stn.corner1), UTIL.xz(stn.corner2)], {
                color: UTIL.convertColor(stn.color),
                opacity: 0.2
            }).addTo(areaLayer)
        
            // Draw text
            new L.marker(UTIL.getCenter(stn.corner1, stn.corner2), { opacity: 0 })
            .bindTooltip(`${stn.name}`, {
                permanent: true,
                direction: 'center',
                className: "stn-label"
            })
            .addTo(areaLayer);
        });
    }
}


async function teleportToSpawn() {
    let resp = await fetch(SETTINGS.BASE_URL + "info");
    if(!resp.ok) return;
    let data = await resp.json();
    if(data.spawnpoint) {
        map.setView(UTIL.xz(data.spawnpoint), 2);
    }
}

function updateOccupy(cid) {
    if(occupyStream) occupyStream.close();
    occupyStream = new EventSource(UTIL.formURL("occupy", map.getCenter().lng, map.getCenter().lat, cid));
    occupyStream.onmessage = (e) => {
        if(e.data.includes("CID")) {
            let continuousId = e.data.split("CID=")[1];
            updateOccupy(continuousId)
        }
    }
    occupyStream.addEventListener("add", (e => {
        let data = JSON.parse(e.data);
        knownOccupants.set(data.uuid, data);
    }));
    occupyStream.addEventListener("remove", (e => {
        knownOccupants.delete(e.data);
    }));
}

function updateLeafletPosLabel() {
    document.getElementById("leaflet-pos").textContent = `${Math.round(map.getCenter().lng)}, ${Math.round(map.getCenter().lat)}`
}

map.on('dragend', function() {
    updateTracks();
    updateTrains();
    updateOccupy();
    updateLeafletPosLabel();
});

function updateTrains(cid) {
    if(trainsStream) trainsStream.close();
    trainsStream = new EventSource(UTIL.formURL("trains", map.getCenter().lng, map.getCenter().lat, cid));
    trainsStream.onmessage = (e) => {
        if(e.data.includes("CID")) {
            let continuousId = e.data.split("CID=")[1];
            updateTrains(continuousId);
        } else {
            let train = JSON.parse(e.data);
            train.lastUpdated = Date.now();
            knownTrains.set(train.uuid, train);
        }
    }
}

(async() => {
    await teleportToSpawn();
    await getAreas();
    updateTracks();
    updateOccupy();
    drawAreas();
    updateTrains();
    updateLeafletPosLabel();

    setInterval(renderTrains, 1000);
    setInterval(renderOccupants, 1000);
})();