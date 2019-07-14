document.onload = setup();

function setup(){
    document.getElementById('calccondsize-button').onclick = calculateConductorSize;
    document.getElementById('voltage').onchange = dataChanged;
    document.getElementById('conduit').onchange = dataChanged;
    document.getElementById('conductor').onchange = dataChanged;
    document.getElementById('sets').onchange = dataChanged;
    document.getElementById('length').oninput = dataChanged;
    document.getElementById('current').oninput = dataChanged;
    document.getElementById('pf').onchange = dataChanged;
    var rad = document.getElementsByName("voltagedrop");
    for(var i = 0; i< rad.length; i++){
        rad[i].onchange = maxvoltagedropChange;
    }
}

function maxvoltagedropChange(e){
    dataChanged(e);
    if(this.id=='maxvoltagedrop') return;
    document.getElementById("maxvoltagedrop").hidden = this.value != 0;
}

function dataChanged(e){
    toggleErrorMsg("", false);
    document.getElementById("results").hidden = true;
}

function calculateConductorSize(e) {
    var rad = document.getElementsByName("voltagedrop");
    var maxVoltageDropValue = 0.0;
    for(var i = 0; i< rad.length; i++){
        if(rad[i].checked) {
            if(rad[i].id == "othervd")
                maxVoltageDropValue = document.getElementById("maxvoltagedrop").value;
            else
                maxVoltageDropValue = rad[i].value;
            break;
        } 
    }

    if(maxVoltageDropValue == "" || maxVoltageDropValue < 0.5 || maxVoltageDropValue > 25){
        showMessage("voltagedroperror", "Voltage drop must be between 0.5 and 25.");
        return;
    }

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
    //maxVoltageDropValue is already defined
    var conduitTypeIndex = document.getElementById("conduit").value;
    var conductorTypeIndex = document.getElementById("conductor").value;
    var sets = document.getElementById("sets").value;
    var pf = document.getElementById("pf").value;

    callApi('GET', '/api/conductorsizingpervoltagedrop' + 
        '?acVoltageIndex=' + acVoltageIndex + 
        '&maxVoltageDropValue=' + maxVoltageDropValue +
        '&conduitTypeIndex=' + conduitTypeIndex +
        '&conductorTypeIndex=' + conductorTypeIndex +
        '&sets=' + sets + 
        '&wireLength=' + wireLength + 
        '&loadCurrent=' + loadCurrent + 
        '&pf=' + pf,
        showResults);
}

function showResults(conductorSizeResults){
    if(conductorSizeResults == null) {
        document.getElementById("results").hidden = true;
        toggleErrorMsg("There is an error in the server side.", true);
        return;
    }
    if(conductorSizeResults.messageContainer.isError) {
        document.getElementById("results").hidden = true;
        toggleErrorMsg(conductorSizeResults.messageContainer.message, true);
        return;
    }

    toggleErrorMsg("", false);
    document.getElementById("cs").innerText = conductorSizeResults.conductorSize;
    document.getElementById("vdp").innerText = conductorSizeResults.voltageDropPercentage.toFixed(2) + "%";
    document.getElementById("ml").innerText = conductorSizeResults.maximumLength.toFixed(0) + " FT (under the above conditions)";
    document.getElementById("results").hidden = false;
}

function toggleErrorMsg(msg, show){
    var m = document.getElementById("message");
    m.innerText = msg;
    m.hidden = !show;
}
