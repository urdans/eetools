document.onload = setup();

function setup(){
    document.getElementById('calcvd-button').onclick = calculateVoltageAtLoad;
    document.getElementById('size').value = 1; //selects wire # 12 initially.
    document.getElementById('size').onchange = updateSets;
    document.getElementById('voltage').onchange = dataChanged;
    document.getElementById('conduit').onchange = dataChanged;
    document.getElementById('conductor').onchange = dataChanged;
    document.getElementById('sets').onchange = dataChanged;
    document.getElementById('length').oninput = dataChanged;
    document.getElementById('current').oninput = dataChanged;
    document.getElementById('pf').onchange = dataChanged;
}

function dataChanged(e){
    toggleErrorMsg("", false);
    document.getElementById("results").hidden = true;
}

function updateSets(e) {
    dataChanged(e);
    var selectSize = document.getElementById("size");
    var selectSets = document.getElementById("sets");
    if(selectSize.value >= 9) selectSets.disabled = false;
    else {
        selectSets.disabled = true;
        selectSets.value = 1;
    }        
}

function calculateVoltageAtLoad(e) {
    var wireLength = document.getElementById("length").value;

    if(wireLength == "" || wireLength <= 0) {
        showMessage("lengtherror", "On way length must be greater than zero!");
        return;
    }
    var loadCurrent = document.getElementById("current").value;
    if(loadCurrent == "" || loadCurrent <= 0) {
        showMessage("currenterror", "Load current must be greater than zero!");
        return;
    }
    var acVoltageIndex = document.getElementById("voltage").value;
    var wireSizeIndex = document.getElementById("size").value;
    var conduitTypeIndex = document.getElementById("conduit").value;
    var conductorTypeIndex = document.getElementById("conductor").value;
    var sets = document.getElementById("sets").value;
    var pf = document.getElementById("pf").value;

    callApi('GET', '/api/calculatevoltageatload' + 
        '?acVoltageIndex=' + acVoltageIndex + 
        '&wireSizeIndex=' + wireSizeIndex +
        '&conduitTypeIndex=' + conduitTypeIndex +
        '&conductorTypeIndex=' + conductorTypeIndex +
        '&sets=' + sets + 
        '&wireLength=' + wireLength + 
        '&loadCurrent=' + loadCurrent + 
        '&pf=' + pf,
        showVoltageDrop);
}

function showVoltageDrop(voltageDropResults){
    if(voltageDropResults == null) {
        document.getElementById("results").hidden = true;
        toggleErrorMsg("There is an error in the server side.", true);
        return;
    }
    if(voltageDropResults.messageContainer.isError) {
        document.getElementById("results").hidden = true;
        toggleErrorMsg(voltageDropResults.messageContainer.message, true);
        return;
    }

    toggleErrorMsg("", false);
    document.getElementById("vl").innerText = voltageDropResults.voltageAtLoad.toFixed(2) + " V";
    document.getElementById("vdv").innerText = voltageDropResults.voltageDropVolts.toFixed(2) + " V";
    document.getElementById("vdp").innerText = voltageDropResults.voltageDropPercentage.toFixed(2) + "%";
    document.getElementById("results").hidden = false;
}

function toggleErrorMsg(msg, show){
    var m = document.getElementById("message");
    m.innerText = msg;
    m.hidden = !show;
}
