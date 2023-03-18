import { SETTINGS } from "./settings.js";

const convertColor = (int) => {
    if(int == null) return "#000000";
    return "#" + Number(int & 0x00FFFFFF).toString(16).padStart(6, "0");
}

const xz = (arr) => {
    if(arr.length == 2) {
        return [arr[1], arr[0]]
    }
    return [arr[2], arr[0]];
}

const xzRound = (arr) => {
    if(arr.length == 2) {
        return [Math.round(arr[1]), Math.round(arr[0])]
    }
    return [Math.round(arr[2]), Math.round(arr[0])];
}

const getCenter = (pos1, pos2) => {
    return [(pos1[1] + pos2[1]) / 2, (pos1[0] + pos2[0]) / 2]
}

const formatRouteVia = (data) => {
    let html = `<br>`;
    for(let rtd of data) {
        html += `<span class="rtvia" style="--rt-color: ${convertColor(rtd.color)}">${rtd.name}</span><br>`
    }
    return html;
}

const manhattenDistance = (x0, y0, x1, y1) => {
    return Math.abs(x1-x0) + Math.abs(y1-y0);
}

const formURL = (endpoint, x, z, cid) => {
    let cidURL = (cid == undefined ? "" : "&cid=" + cid);
    return SETTINGS.BASE_URL + endpoint + "?x=" + Math.round(x) + "&z=" + Math.round(z) + cidURL;
}

export const UTIL = {
    convertColor: convertColor,
    manhattenDistance: manhattenDistance,
    getCenter: getCenter,
    xz: xz,
    xzRound: xzRound,
    formatRouteVia: formatRouteVia,
    formURL: formURL
}