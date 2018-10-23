/**
 *  Intermatic PE653 Pool Control System
 *
 *  Original Copyright 2014 bigpunk6
 *  Updated 2016 - 2018 KeithR26
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  This DTH is a "Composite Device Type Handler" which supports multiple "Child" devices that appear in
 *	the "Things" list and can be used by SmartApps to control the 5 switches, Pool/Spa mode and 4 VSP speeds.
 *	This requires a second DTH be installed: erocm123 / Switch Child Device
 *
 *  Don't use SamrtThings Multi-channel (deprecated) or Cooper Lee's code (vTile_ms, ms_w_vts). These are incompatible
 *  with the Composite DTH architecture.
 *
 *  This code was primarily written by bigpunk6 and KeithR26.
 *  Semantic versioning and further adaptation to the Hubitat system by Tooluser
 *  See https://github.com/HubitatCommunity/hubitat-zwavemultichannel-pe653 for version information.
 *
*/
def getVERSION () {"Ver 3.1.0a"}		// Keep track of handler version

metadata {
	definition (name: "Intermatic Pool Control System", author: "Tooluser", namespace:  "Nowhereville") {
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Configuration"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Zw Multichannel"
		capability "Thermostat"
		//capability "Update"

		attribute "operationMode", "string"
		attribute "firemanTimeout", "string"
		attribute "temperature", "string"
		attribute "temperatureOffsets", "string"
		attribute "poolspaConfig", "string"
		attribute "poolSetpoint", "string"
		attribute "spaSetpoint", "string"
		attribute "poolSpaMode", "string"
		attribute "lightColor", "string"
		attribute "lightCircuits", "string"
		attribute "ccVersions", "string"
		attribute "VersionInfo", "string"
		attribute "ManufacturerInfo", "string"
		attribute "debugLevel", "string"
		attribute "switch1", "string"
		attribute "switch2", "string"
		attribute "switch3", "string"
		attribute "switch4", "string"
		attribute "switch5", "string"
		attribute "swVSP1", "string"
		attribute "swVSP2", "string"
		attribute "swVSP3", "string"
		attribute "swVSP4", "string"
		attribute "schedules", "JSON_OBJECT"
		attribute "lastScheduleChg", "string"
		attribute "VSPSpeeds", "string"

		command "poll"
		command "quickSetPool", ["number"]
		command "quickSetSpa", ["number"]
		command "quickGetWaterTemp"
		command "quickGetWaterTempOld"
		command "quickGetTestCmds"
		command "setPoolMode"
		command "setSpaMode"
		command "togglePoolSpaMode"
		command "childOn"
		command "childOff"
		command "on1"
		command "off1"
		command "on2"
		command "off2"
		command "on3"
		command "off3"
		command "on4"
		command "off4"
		command "on5"
		command "off5"
		command "setVSPSpeed", ["number"]
		command "setVSPSpeed0"
		command "setVSPSpeed1"
		command "setVSPSpeed2"
		command "setVSPSpeed3"
		command "setVSPSpeed4"
		command "setLightColor", ["number"]
		command "setColor"
		command "setClock"
		command "getSchedules", ["number"]
		command "setSchedule", ["number","number","number","number","number","number"]
		command "resetSchedule", ["number","number"]
		command "setVSPSpeeds"
		command "insertLogTrace"
		command "executeArbitraryCommand", ["string"]
		//command "updated"

		fingerprint deviceId: "0x1001", inClusters: "0x91,0x73,0x72,0x86,0x81,0x60,0x70,0x85,0x25,0x27,0x43,0x31", outClusters: "0x82"
	}

	preferences {
		input "operationMode1", "enum", title: "Booster/Cleaner Pump",
			options:[[1:"No"],
					 [2:"Uses Circuit-1"],
					 [3:"Variable Speed pump Speed-1"],
					 [4:"Variable Speed pump Speed-2"],
					 [5:"Variable Speed pump Speed-3"],
					 [6:"Variable Speed pump Speed-4"]]
		input "operationMode2", "enum", title: "Pump Type",
			options:[[0:"1 Speed Pump without Booster/Cleaner"],
					 [1:"1 Speed Pump with Booster/Cleaner"],
					 [2:"2 Speed Pump without Booster/Cleaner"],
					 [3:"2 Speed Pump with Booster/Cleaner"],
					 [4:"Variable Speed Pump without Booster/Cleaner"],
					 [5:"Variable Speed Pump with Booster/Cleaner"],
					 [6:"Reserved 6"],
					 [7:"Reserved 7"]]
		input "poolSpa1", "enum", title: "Pool or Spa",
			options:[[0:"Pool"],
					 [1:"Spa"],
					 [2:"Both"]]
		input "fireman", "enum", title: "Fireman Timeout",
			options:[["255":"No heater installed"],
					 ["0":"No cool down period"],
					 ["1":"1 minute"],
					 ["2":"2 minute"],
					 ["3":"3 minute"],
					 ["4":"4 minute"],
					 ["5":"5 minute"],
					 ["6":"6 minute"],
					 ["7":"7 minute"],
					 ["8":"8 minute"],
					 ["9":"9 minute"],
					 ["10":"10 minute"],
					 ["11":"11 minute"],
					 ["12":"12 minute"],
					 ["13":"13 minute"],
					 ["14":"14 minute"],
					 ["15":"15 minute"]]
		input "tempOffsetwater", "number", title: "Water temperature offset", range: "-5..5", defaultValue: 0, required: true
		input "tempOffsetair", "number",
			title: "Air temperature offset - Sets the Offset of the air temperature for the add-on Thermometer in degrees Fahrenheit -5F to +5F", range: "-5..5", defaultValue: 0, required: true
		input "debugLevel", "enum", title: "Debug Level", multiple: "true",
			options:[[0:"Off"],
					 [1:"Low"],
					 [2:"High"]], defaultvalue: 0
		input "ZWdelay", "number",
			title: "Delay between Z-Wave commands sent (milliseconds). Suggest 1000.", defaultValue: 1000, required: true
		input "C1ColorEnabled", "enum", title: "Circuit 1 Color Light Enable:", defaultValue: 0,
			options:[[0:"off"],
					 [1:"On"]]
		input "C2ColorEnabled", "enum", title: "Circuit 2 Color Light Enable:", defaultValue: 0,
			options:[[0:"off"],
					 [1:"On"]]
		input "C3ColorEnabled", "enum", title: "Circuit 3 Color Light Enable:", defaultValue: 0,
			options:[[0:"off"],
					 [1:"On"]]
		input "C4ColorEnabled", "enum", title: "Circuit 4 Color Light Enable:", defaultValue: 0,
			options:[[0:"off"],
					 [1:"On"]]
		input "C5ColorEnabled", "enum", title: "Circuit 5 Color Light Enable:", defaultValue: 0,
			options:[[0:"off"],
					 [1:"On"]]
	}

	// tile definitions
	tiles(scale: 2) {
		valueTile("mainTile", "device.temperature", width: 2, height: 2, inactiveLabel: true ) {
			state "temperature", label:'${currentValue}°', action: "quickGetWaterTemp",icon: "st.Health & Wellness.health2",
					backgroundColors:[
						[value: 32, color: "#153591"],
						[value: 54, color: "#1e9cbb"],
						[value: 64, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 90, color: "#f1d801"],
						[value: 98, color: "#d04e00"],
						[value: 110, color: "#bc2323"]
					]
		}
		valueTile("temperatureTile", "device.temperature", width: 2, height: 2, inactiveLabel: true ) {
			state "temperature", label:'${currentValue}°', action: "quickGetWaterTemp",
					backgroundColors:[
						[value: 32, color: "#153591"],
						[value: 54, color: "#1e9cbb"],
						[value: 64, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 90, color: "#f1d801"],
						[value: 98, color: "#d04e00"],
						[value: 110, color: "#bc2323"]
					]
		}
		valueTile("airTempFTile", "device.airTempFreeze", width: 1, height: 1, inactiveLabel: true ) {
			state "airTemp", label:'${currentValue}°',
					backgroundColors:[
						[value: 0,  color: "#ffffff"],
						[value: 32, color: "#153591"],
						[value: 54, color: "#1e9cbb"],
						[value: 64, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 90, color: "#f1d801"],
						[value: 98, color: "#d04e00"],
						[value: 110, color: "#bc2323"]
					]
		}
		valueTile("airTempSTile", "device.airTempSolar", width: 1, height: 1, inactiveLabel: true ) {
			state "airTemp", label:'${currentValue}°',
					backgroundColors:[
						[value: 0,  color: "#ffffff"],
						[value: 32, color: "#153591"],
						[value: 54, color: "#1e9cbb"],
						[value: 64, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 90, color: "#f1d801"],
						[value: 98, color: "#d04e00"],
						[value: 110, color: "#bc2323"]
					]
		}
		valueTile("airTempLabel", "device.airTempLabel", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "airTemp", label:'  AIR: Frz/Sol', action:"quickGetTestCmds", backgroundColor:"#ffffff"
		}
		controlTile("poolSliderControl", "device.poolSetpoint", "slider", width: 2, height: 1, inactiveLabel: false, range:"(39..104)") {
			state "PoolSetpoint", action:"quickSetPool", backgroundColor:"#d04e00"
		}
		valueTile("poolSetpoint", "device.poolSetpoint", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "pool", label:' POOL:', backgroundColor:"#ffffff"
		}
		controlTile("spaSliderControl", "device.spaSetpoint", "slider", width: 2, height: 1, inactiveLabel: false, range:"(39..104)") {
			state "SpaSetpoint", action:"quickSetSpa", backgroundColor: "#1e9cbb"
		}
		valueTile("spaSetpoint", "device.spaSetpoint", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "spa", label:'   SPA:', backgroundColor:"#ffffff"
		}
		controlTile("lightColorSliderControl", "device.lightColor", "slider", width: 2, height: 1, inactiveLabel: false, range:"(1..14)") {
			state "color", action:"setLightColor", backgroundColor:"#d04e00"
		}
		valueTile("lightColor", "device.lightColorBtn", width: 1, height: 1, inactiveLabel: true, decoration: "flat") {
			state "color", action:"setColor", label:'COLOR:', backgroundColor:"#ffffff"
		}

		standardTile("poolSpaMode", "device.poolSpaMode", width: 2, height: 2, decoration: "flat") {
			state "on",         label: "",           action: "setPoolMode", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/spa.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "off",        label: "",           action: "setSpaMode",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/Pool.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn",  label: 'changing',   action: "setPoolMode",        icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/spa.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label: 'changing',   action: "setSpaMode",         icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/Pool.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "disabled",   label:'',            icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/all-white.png", backgroundColor: "#ffffff"
		}
		standardTile("switch1", "device.switch1", width: 1, height: 1, decoration: "flat") {
			state "on",         label: "on",         action: "off1", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw1-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "off",        label: "off",        action: "on1",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw1-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn",  label:'Turning on',  action: "off1", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw1-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "on1",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw1-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
		}
		standardTile("switch2", "device.switch2", width: 1, height: 1, decoration: "flat") {
			state "on",         label: "on",         action: "off2", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw2-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "off",        label: "off",        action: "on2",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw2-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn",  label:'Turning on',  action: "off2", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw2-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "on2",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw2-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
		}
		standardTile("switch3", "device.switch3", width: 1, height: 1, decoration: "flat") {
			state "on",         label: "on",         action: "off3", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw3-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "off",        label: "off",        action: "on3",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw3-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn",  label:'Turning on',  action: "off3", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw3-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "on3",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw3-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
		}
		standardTile("switch4", "device.switch4", width: 1, height: 1, decoration: "flat") {
			state "on",         label: "on",         action: "off4", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw4-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "off",        label: "off",        action: "on4",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw4-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn",  label:'Turning on',  action: "off4", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw4-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "on4",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw4-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
		}
		standardTile("switch5", "device.switch5", width: 1, height: 1, decoration: "flat") {
			state "on",         label: "on",         action: "off5", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw5-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "off",        label: "off",        action: "on5",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw5-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn",  label:'Turning on',  action: "off5", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw5-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "on5",  icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/sw5-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
		}
		standardTile("swVSP1", "device.swVSP1", width: 1, height: 1, decoration: "flat") {
			state "off",        label: "off",        action: "setVSPSpeed1", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp1-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "on",         label: "on",         action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp1-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOn",  label:'Turning on',  action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp1-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "setVSPSpeed1", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp1-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "disabled",   label:'',                                    icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/all-white.png",backgroundColor: "#ffffff"
		}
		standardTile("swVSP2", "device.swVSP2", width: 1, height: 1, decoration: "flat") {
			state "off",        label: "off",        action: "setVSPSpeed2", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp2-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "on",         label: "on",         action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp2-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOn",  label:'Turning on',  action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp2-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "setVSPSpeed2", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp2-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "disabled",   label:'',                                    icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/all-white.png",backgroundColor: "#ffffff"
		}
		standardTile("swVSP3", "device.swVSP3", width: 1, height: 1, decoration: "flat") {
			state "off",        label: "off",        action: "setVSPSpeed3", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp3-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "on",         label: "on",         action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp3-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOn",  label:'Turning on',  action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp3-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "setVSPSpeed3", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp3-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "disabled",   label:'',                                    icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/all-white.png",backgroundColor: "#ffffff"
		}
		standardTile("swVSP4", "device.swVSP4", width: 1, height: 1, decoration: "flat") {
			state "off",        label: "off",        action: "setVSPSpeed4", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp4-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "on",         label: "on",         action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp4-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOn",  label:'Turning on',  action: "setVSPSpeed0", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp4-on.png",  backgroundColor: "#79b821", nextState: "turningOff"
			state "turningOff", label:'Turning off', action: "setVSPSpeed4", icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/vsp4-off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "disabled",   label:'',                                    icon: "https://raw.githubusercontent.com/KeithR26/Intermatic-PE653/master/all-white.png",backgroundColor: "#ffffff"
		}
		standardTile("blank1", "device.blank", width: 2, height: 1, decoration: "flat") {
			state "on",         icon: "st.Health & Wellness.health2",  backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("insertLogTrace", "something", width: 6, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Insert Trace in Log", action: "insertLogTrace"
		}
		valueTile("clock", "device.clock", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "clockName", label:'${currentValue}', backgroundColor:"#ffffff", action: "setClock"
		}
		valueTile("heaterLabel", "device.heaterLabel", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "airTemp", label:'  HEAT:', backgroundColor:"#ffffff"
		}
		standardTile("heaterTile", "device.heater", width: 2, height: 1, canChangeIcon: true) {
			state "off",        label: "heater off",   icon: "st.Health & Wellness.health2",  backgroundColor: "#ffffff"
			state "on",         label: "*HEATING*",  icon: "st.Health & Wellness.health2", backgroundColor: "#bc2323"
		}
		// standardTile("configure", "device.configure", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
		// 	state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		// }
		standardTile("blank3", "device.blank3", width: 1, height: 1, decoration: "flat") {
			state "on",     label: "", icon: "st.Health & Wellness.health2",  backgroundColor: "#ffffff"
		}

	main "mainTile"
		details([
			"blank3",
			"switch1","switch2","switch3","switch4","switch5",
			"poolSpaMode",
			"temperatureTile",
			"swVSP1","swVSP2","swVSP3","swVSP4",
			"poolSetpoint", "poolSliderControl",
			"spaSetpoint", "spaSliderControl",
			"lightColor","lightColorSliderControl",
			"heaterLabel", "heaterTile",
			"airTempLabel", "airTempFTile", "airTempSTile",
			"refresh",
			"clock",
			// "blank1",
			// "configure",
			])
	}
}

// Constants for PE653 configuration parameter locations
def getDELAY () {ZWdelay}								// How long to delay between commands to device (configured)
def getMIN_DELAY () {"800"}								// Minimum delay between commands to device (configured)
def getSWITCH_SCHED_PARAM (int sw, int sch) { (4 + (sw-1)*3 + (sch-1)) }	// Configuration schedule for switch 1-5
def getVSP_RPM_SCHED_PARAM (int sp) { (32 + (sp-1))}	// Configuration schedule for VSP RPM Speeds 1-4
def getVSP_RPMMAX_SCHED_PARAM () { (49) }				// VSP RPM Max speed Schedule 0x31
def getPOOL_SPA_SCHED_PARAM (int sch) { 19 + (sch-1) }	// Pool/Spa mode Schedule 1-3 - 0x13
def getPOOL_SPA_CONFIG () { 22 }						// Pool/Spa mode Mode - 0x16
def getVSP_SCHED_PARAM (int sp, int sch) { (36 + (sp-1)*3 + (sch-1)) }		// VSP Speed 1-4 Schedule 1-3 - 0x24
def getPOOL_SPA_CHAN () { 39 }							// Pool/Spa channel - 0x27
def getPOOL_SPA_EP () { 6 }								// Pool/Spa endpoint - 6
def getVSP_SPEED (int sched) { ((sched - 35) / 3) }		// Convert from sched to speed
def getVSP_CHAN_NO (int spd) { (16 + (spd - 1)) }		// VSP Speed 1 Channel  - 0x10 - 0x13
def getVSP_EP (int spd) { (6 + spd) }					// VSP Endpoint 7 - 10
def getVSP_SPEED_FROM_CHAN (int chan) { ((chan - 16) + 1) }	// Convert from channel to speed - 0x10
def getVSP_ENABLED () { (operationMode2 >= "4") ? 1 : 0 }	// True if a Variable Speed Pump Configured
def getPOOL_SPA_COMBO () { (poolSpa1 == "2") ? 1 : 0 }	// True if both Pool and Spa
def getSWITCH_NAME (int instance) {
	def swNames = ["switch1","switch2","switch3","switch4","switch5","poolSpaMode","swVSP1","swVSP2","swVSP3","swVSP4"]
	return swNames[instance - 1]
}
def getSCHED_PARAM (int ep, int sch) {
	if (ep >= 1 && ep <= 5)  {return getSWITCH_SCHED_PARAM(ep, sch)}
	if (ep == 6)             {return getPOOL_SPA_SCHED_PARAM(sch)}
	if (ep >= 7 && ep <= 10) {return getVSP_SCHED_PARAM(ep-6, sch)}
	return 0
}
def getEP_FROM_SCHED_PARM (int paramNum) {
	if (paramNum >= getSCHED_PARAM (1, 1) && paramNum <= getSCHED_PARAM (5, 3))   {return ((paramNum - getSCHED_PARAM(1,1)).intdiv(3) + 1)}
	if (paramNum >= getSCHED_PARAM (6, 1) && paramNum <= getSCHED_PARAM (6, 3))   {return 6}
	if (paramNum >= getSCHED_PARAM (7, 1) && paramNum <= getSCHED_PARAM (10, 3))  {return ((paramNum - getSCHED_PARAM(7,1)).intdiv(3) + 1 + 6)}
	return 0
}

// Return the list supported command classes by PE653. The following are the versions for firmware v3.4
// ccVersions: {"20":1,"25":1,"27":1,"31":1,"43":1,"60":2,"70":1,"72":1,"73":1,"81":1,"85":1,"86":1,"91":1}
def getSupportedCmdClasses () {[
	0x20,	//	Basic
	0x25,	//	Switch Binary
	0x27,	//	Switch All
	0x31,	//	Sensor Multilevel
	0x43,	//	Thermostat setpoint
	0x60,	//	Multi Instance
	0x70,	//	Configuration
	0x72,	//	Manufacturer Specific
	0x73,	//	Powerlevel
	0x81,	//	Clock
	0x85,	//	Association
	0x86,	//	Version
	0x91	//	Manufacturer Proprietary
	]
}

// Main entry point for messages from the device
def parse(String description) {
	log("TRACE", 2, "<<< Incoming: [$description]")
	def result = null
	def command = null
	def cmd = null
	def payloadStr = ""
	byte[] payload = []
	if (description.startsWith("Err")) {
		log("WARN", "Error in Parse")
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		try {
			def command1 = description.split('command:')[1]
			command = command1.split(',')[0]
			payloadStr = description.split('payload:')[1]
			} catch (e) {
				log("ERROR", "..... Exception in Parse() ${cmd} - description:${description} exception ${e}")
			}
			// log("DEBUG", "command: ${command}   payloadStr: ${payloadStr}")
		if (command.contains("9100")) {
			payload = payloadStr.replace(" ","").decodeHex()
			log("DEBUG", 0, ">>>>> unParsed cmd - description:$description ")
			result = zwaveEventManufacturerProprietary(payload, payloadStr)
		} else {
			try {
				// cmd = zwave.parse(description, [0x20: 1, 0x25:1, 0x27:1, 0x31:1, 0x43:1, 0x60:3, 0x70:2, 0x72:1, 0x81:1, 0x85:2, 0x86: 1, 0x73:1, 0x91:1])
				cmd = zwave.parse(description, [0x20: 1, 0x25:1, 0x27:1, 0x31:1, 0x43:1, 0x60:1, 0x70:2, 0x72:1, 0x81:1, 0x85:2, 0x86: 1, 0x73:1, 0x91:1])
			} catch (e) {
				log("WARN", "..... Exception in zwave.parse() ${cmd} - description:${description} exceptioon ${e}")
			}
			if (cmd) {
				log("DEBUG", 2, "----- ${cmd} - description:$description ")
				result = zwaveEvent(cmd)
			} else {
				log("WARN", "----- Parse() parsed to NULL:  description:$description")
				return null
			}
		}
	}
	delayResponseLog(result)
}

//Reports

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
	state.VersionInfo = "Versions: Firmware v${cmd.applicationVersion}.${cmd.applicationSubVersion}   DTH: ${VERSION}   zWaveLibraryType: ${cmd.zWaveLibraryType}    zWaveProtocol: v${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(name: "VersionInfo", value: state.VersionInfo, displayed: true, descriptionText: state.VersionInfo)
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionCommandClassReport cmd) {
	if (cmd.commandClassVersion) {
		def cls = String.format("%02X", cmd.requestedCommandClass)
		state.ccVersions[cls] = cmd.commandClassVersion
		createEvent(name: "ccVersions", value: util.toJson(state.ccVersions), displayed: false, descriptionText:"")
	} else {
		[]
	}
}

def zwaveEvent(hubitat.zwave.commands.clockv1.ClockReport cmd) {
	def time1 = ""
	time1 = "${String.format("%02d",cmd.hour)}:${String.format("%02d",cmd.minute)}"
	log("DEBUG", "ClockReport: from PE653=${time1}")
	createEvent(name: "clock", value: "${time1}", displayed: false, descriptionText: "PE653 Clock: ${time1}")
}

def zwaveEvent(hubitat.zwave.commands.thermostatsetpointv1.ThermostatSetpointSupportedReport cmd) {
	log("DEBUG", "thermostatSetpointSupportedReport !!!")
	def cmds = []
	cmds
}

def zwaveEventManufacturerProprietary(byte [] payload, payloadStr) {
	log("DEBUG", "ManufacturerProprietary event, [1]:${String.format("%02X",payload[1])}  [4]:${String.format("%02X",payload[4])}  payload: ${payloadStr}")

	def rslt = []
	byte [] oldResp  = [1,2,3,4]
	def respType = 0
	def diffCnt = 0
	def oldP = ""
	def newP = ""
	def oldD = ""
	def newD = ""
	def head = ""
	if (payload[1] == 0x40 && payload[4] == -124) {
		respType = 84
		oldResp = state.manProp1
		if (oldResp == null) {oldResp = payload}
		state.manProp1 = payload
		rslt = process84Event(payload)
	} else if (payload[1] == 0x40 && payload[4] == -121) {
		respType = 87
		oldResp = state.manProp2
		if (oldResp == null) {oldResp = payload}
		state.manProp2 = payload
		rslt = process87Event(payload)
	} else if (payload[1] == 0x41) {
		respType = 41
		oldResp = state.manProp3
		if (oldResp == null) {oldResp = payload}
		state.manProp3 = payload
		// rslt = process41Event(payload)
	} else {
		log("WARN", "Unexpected ManufacturersProprietary event received !!")
	}
	// log("DEBUG", "respType:${respType}  oldResp: ${oldResp}")
	if (oldResp == null) {oldResp = (byte[])[0,1,2,3,4] as byte [];log.debug "==null forced to array"}
	for (def i=0;i<payload.length;i++) {
		oldP += " ${String.format("%02X",oldResp[i])}"
		newP += " ${String.format("%02X",payload[i])}"
		oldD += " ${String.format("%03d",oldResp[i])}"
		newD += " ${String.format("%03d",payload[i])}"
		if (oldResp[i] != payload[i] && (
			(respType == 84 && (i != CLOCK_MINUTE_84 && i != CLOCK_HOUR_84)) ||
			(respType == 87 && (i != CLOCK_MINUTE_87 && i != CLOCK_HOUR_87)) ||
			(respType == 41 && (i != 99))
		)) {
			diffCnt++
			head += " ${String.format("%02d",i)} "
		} else {
			head += "--- "
		}
	}
	if (diffCnt) {
		log("debug", 2, "respType:${respType}  differences:${diffCnt}\n__ __ ${head}\nnew-:   ${newP}\nold-- :   ${oldP}")
	}
	rslt
}

def getCLOCK_MINUTE_84 ()     { 16 }			// Clock Minute
def getCLOCK_HOUR_84 ()       { 15 }				// Clock Hour
def getWATER_TEMP_84 ()       { 12 }				// Water Temperature
def getAIR_TEMP_FREEZE_84 ()  { 13 }			// Air Temperature for Freeze sensing
def getAIR_TEMP_SOLAR_84 ()   { 14 }			// Air Temperature for Solar control
def getSWITCHES_84 ()         { 8 }					// Bit mask of 5 switches. SW1 = 01X, SW5 = 10X
def getPOOL_SPA_MODE_84 ()    { 11 }			// Pool/Spa mode. 01x Pool mode, 00x Spa mode
def getVSP_SPEED_84 ()        { 20 }				// VSP Speed bit mask. 01x = VSP1, 08x = VSP4

// Received a ManufacturerProprietary message. Pull the important details and update the UI controls

private process84Event(byte [] payload) {
	log("DEBUG", "+++++ process84Event payload: ${payload}")
	def rslt = []
	def map = [:]
	def str = ""
	def val = 0
	def ch = 0
	Integer temp

	def swMap = ['1':1, '2':2, '3':4, '4':8, '5':16]
	log("DEBUG", "payload for switches is ${payload[SWITCHES_84]}")
	for (sw in swMap) {
		if (payload[SWITCHES_84] & sw.value) {
			val = 0xFF
		} else {
			val = 0
		}
		rslt.addAll(createMultipleEvents(sw.key.toInteger(), val, (val == 0) ? "off": "on"))
	}

	//	Set VSP indicators if enabled
	if (VSP_ENABLED) {
		for (vsp in ['1':1, '2':2, '3':4, '4':8]) {
			if (payload[VSP_SPEED_84] & vsp.value) {
				val = 0xFF
			} else {
				val = 0
			}
			ch = getVSP_EP(vsp.key.toInteger())
			rslt.addAll(createMultipleEvents(ch, val, (val == 0) ? "off": "on"))
		}
	}

	//	Set Pool/Spa mode indicator
	if (POOL_SPA_COMBO) {
		val = ((payload[POOL_SPA_MODE_84] & 0x01) == 0) ? 0xFF : 0
		rslt.addAll(createMultipleEvents(POOL_SPA_EP, val, (val == 0) ? "off": "on"))
	}

	//	Update Water Temperature
	temp = payload[WATER_TEMP_84] >= 0 ? payload[WATER_TEMP_84] : payload[WATER_TEMP_84] + 255
	rslt << createEvent(name: "temperature", value: temp, unit: "F", displayed: true, isStateChange: true)

	//	Update Freeze Air Temperature
	//	payload[AIR_TEMP_FREEZE_84] = -127  // test for negative
	temp = payload[AIR_TEMP_FREEZE_84] >= 0 ? payload[AIR_TEMP_FREEZE_84] : payload[AIR_TEMP_FREEZE_84] + 255
	rslt << createEvent(name: "airTempFreeze", value: temp, unit: "F", displayed: true, isStateChange: true)

	//	Update Solar Air Temperature
	//	payload[AIR_TEMP_SOLAR_84] = -125   // Test for negative
	temp = payload[AIR_TEMP_SOLAR_84] >= 0 ? payload[AIR_TEMP_SOLAR_84] : payload[AIR_TEMP_SOLAR_84] + 255
	rslt << createEvent(name: "airTempSolar", value: temp, unit: "F", displayed: true, isStateChange: true)

	//	Update Clock
	def time1 = "${String.format("%02d",payload[CLOCK_HOUR_84])}:${String.format("%02d",payload[CLOCK_MINUTE_84])}"
	rslt << createEvent(name: "clock", value: "${time1}", displayed: false, descriptionText: "PE653 Clock: ${time1}")

	rslt
}

private getHEATER_87 () { 15 }					// Heater. 04x = on, 00x = off
private getCLOCK_MINUTE_87 () { 25 }			// Clock Minute
private getCLOCK_HOUR_87 () { 24 }				// Clock Hour

// Received a ManufacturerProprietary message. Pull the important details and update the UI controls

private process87Event(byte [] payload) {
	log("DEBUG", "+++++ process87Event payload: ${payload}")
	def rslt = []
	def val = ((payload[HEATER_87] & 0x04) == 0) ? "off" : "on"
	rslt << createEvent(name: "heater", value: "$val", isStateChange: true, displayed: true, descriptionText: "Heater is ${val}")

	rslt
}

// ManufacturerSpecificReport(manufacturerId: 5, manufacturerName: Intermatic, productId: 1619, productTypeId: 20549)
def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
	state.ManufacturerInfo = "ManufacturerInfo:  manufacturerId: $cmd.manufacturerId, manufacturerName: $cmd.manufacturerName, productId: $cmd.productId, productTypeId: $cmd.productTypeId"
	log("DEBUG", "ManufacturerSpecificReport:  ${state.ManufacturerInfo}")
	createEvent(name: "ManufacturerInfo", value: state.ManufacturerInfo, displayed: true, descriptionText: state.ManufacturerInfo)
}

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def map = [:]
	def cmds = []
	def myValue = ""
	def externalValue = ""
	def paramNum = cmd.parameterNumber
	int ep = getEP_FROM_SCHED_PARM(paramNum)
	int sch = paramNum - getSCHED_PARAM(ep,1) +1
	//	log("DEBUG", "paramNum=$paramNum   getSCHED_PARAM($ep,1)=${getSCHED_PARAM(ep,1)}   sch=${sch}")
	map.value = cmd.configurationValue[0]
	map.name = ""
	map.displayed = true
	switch (paramNum) {
		case 1:
			map.name = "operationMode"
			cmds << createEvent(map)
			break;
		case 2:
			map.name = "firemanTimeout"
			cmds << createEvent(map)
			break;
		case 3:
			map.name = "temperatureOffsets"
			cmds << createEvent(map)
			break;
		case POOL_SPA_CONFIG:
			map.name = "spamode1"
			// log("DEBUG", "..... ConfigurationReport  - got parameter=${paramNum}")
			// cmds << createEvent(map)
			break;
		default:
			// log("WARN", "..... ConfigurationReport  - UNKNOWN parameter=${paramNum}")
			break;
	}

	// If this configuration report corresponds to one of the schedules, record the current setting and create events
	if (ep) {
		def lst
		def schLst
		if (cmd.size == 4 && cmd.configurationValue[0] == 0xFF && cmd.configurationValue[1] == 0xFF && cmd.configurationValue[2] == 0xFF && cmd.configurationValue[3] == 0xFF) {
		// log("DEBUG", "schedule $ep,$sch is null")
			lst = null
		} else {
			lst = [ep, sch]
			for (def i=0;i<=2;i+=2) {
				int tim = cmd.configurationValue[i+1] * 256 + cmd.configurationValue[i]
				lst << tim.intdiv(60)
				lst << tim % 60
			}
		}
		if (state.schedules == null) {state.schedules = []}
		schLst = state.schedules[ep]
		if (schLst == null) {schLst =[ep];state.schedules[ep]=schLst }
		schLst[sch] = lst
		log("DEBUG", "ConfigurationReport for Schedule: ep:${ep} sch=${sch} lst=${lst} schLst=${schLst}")
		// log("DEBUG", "schLst=$schLst  state.schedules=${state.schedules}")
		// log("DEBUG", "state = ${state}")

		cmds << createEvent(name: "schedules", value: util.toJson(state.schedules), isStateChange: true, displayed: true, descriptionText: "Schedule fpr ep=${ep} sch=${sch} updated to: ${lst}")
		cmds << createEvent(name: "lastScheduleChg", value: ep, isStateChange: true, displayed: true, descriptionText: "Schedule ${ep},${sch} changed")
	}
	cmds
}

def zwaveEvent(hubitat.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd) {
	def map = [:]
	map.value = cmd.scaledSensorValue.toString()
	map.unit = cmd.scale == 1 ? "F" : "C"
	map.displayed = true
	map.isStateChange = true
	map.descriptionText = "Water Temp is ${map.value}°${map.unit}"
	map.name = "temperature"
	createEvent(map)
}

def zwaveEvent(hubitat.zwave.commands.thermostatsetpointv1.ThermostatSetpointReport cmd) {
	def map = [:]
	def setpoint
	double requestedSetpoint = 0
	map.value = cmd.scaledValue.toString()
	// map.unit = cmd.scale == 1 ? "F" : "C"
	map.unit = "F"
	map.displayed = true
	switch (cmd.setpointType) {
		case 1:
			map.name = "poolSetpoint"
			setpoint = state.poolSetpointTemp
			break;
		case 7:
			map.name = "spaSetpoint"
			setpoint = state.spaSetpointTemp
			break;
		default:
			return [:]
	}
	log("DEBUG", "ThermostatSetpointReport: incoming state.scale=${state.scale} requested state.${map.name}Temp=${setpoint}  reported Setpoint=${cmd.scaledValue}")

	if (setpoint != null) {requestedSetpoint = setpoint}
	// So we can respond with same format
	state.size = cmd.size
	state.precision = cmd.precision
//	state.scale = cmd.scale
	// PE653 ver3.4 has two varients: v34 that requires Celsius scale (0) and a newer (correct) V34.14 that requires Fahrenheit scale (1).
	// Unfortunately the version report does not distinguish between the two versions.
	// Regardless of scale, PE653 always expects the actual temperature value to be in Fahrenheit.
	// If a setpoint Set request is ignored, then flip to the opposite scale in an attempt to adapt to the one this firmware expects.
	if (cmd.scaledValue != requestedSetpoint) {
		def oldScale = state.scale
		state.scale = state.scale == 1 ? 0 : 1
		log("WARN", "ThermostatSetpointReport: Requested Setpoint was ignored. Switching from old scale=${oldScale} to new state.scale=${state.scale}")
	}
	log("DEBUG", "ThermostatSetpointReport: outgoing state.scale=${state.scale}")
	createEvent(map)
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	if (cmd.value == 0) {
		createEvent(name: "switch", value: "off")
	} else if (cmd.value == 255) {
		createEvent(name: "switch", value: "on")
	}
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
	log("DEBUG", "+++++ BasicSet cmd=${cmd}")
	def result = []
	if (cmd.value == 0) {
		result = createEvent(name: "switch", value: "off")
	} else if (cmd.value == 255) {
		result = createEvent(name: "switch", value: "on")
	}
	result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicGet cmd) {
	log("DEBUG", "+++++ BasicGet cmd=${cmd}")
	def cmds = []
	int val = device.currentValue("switch1").equals("on") ? 0xFF : 0
	cmds << zwave.basicV1.basicReport(value: val)
	delayBetweenLog(cmds)
}

/*
def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiInstanceReport cmd) {
	log("DEBUG", "MultiInstanceReport cmd=${cmd}")
}
def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
	log("DEBUG", "MultiChannelEndPointReport cmd=${cmd}")
}
*/

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) { [] }

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationGroupingsReport cmd) { [] }

// Multi-channel event from the device. Version 1 of Command Class
def zwaveEvent(hubitat.zwave.commands.multiinstancev1.MultiInstanceCmdEncap cmd) {
	log("DEBUG", "+++++ multiinstancev1.MultiInstanceCmdEncap cmd=${cmd}")
	zwaveEventMultiCmdEncap(cmd)
}

/*
// Multi-channel event from the device. Version 3 of Command Class
def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiInstanceCmdEncap cmd) {
	log("WARN", "multichannelv3.MultiInstanceCmdEncap cmd=${cmd}")
	zwaveEventMultiCmdEncap(cmd)
}
*/

// Multi-channel event from the device, common method.
def zwaveEventMultiCmdEncap(cmd) {
	log("DEBUG", "+++++ zwaveEventMultiCmdEncap cmd=${cmd}")
	def rslt = []
	def String myStr = (cmd.parameter[0] == 0) ? "off": "on"
	def sw = 0
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (encapsulatedCommand) {
		switch(cmd.instance) {
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				sw = cmd.instance
				break;

			case POOL_SPA_CHAN:
				sw = POOL_SPA_EP
				break;

			case getVSP_CHAN_NO(1):
			case getVSP_CHAN_NO(2):
			case getVSP_CHAN_NO(3):
			case getVSP_CHAN_NO(4):
				sw = (cmd.instance - getVSP_CHAN_NO(1)) + 7
				break;
			default:
				log("WARN", "..... MultiInstanceCmdEncap  - UNKNOWN INSTANCE=${cmd.instance}")
				return []
		}

		rslt.addAll(createMultipleEvents(sw, cmd.parameter[0], myStr))
	} else {
		log("WARN", "MultiInstanceCmdEncap: Could not de-encapsulate!!!")
	}
	rslt
}

// Used to update our own switches state as well as the child devices
// Two Events: One event is immediately sent to the child device and another is returned to our own UI control
private List createMultipleEvents (Integer endpoint, Integer externalParm, String myParm) {
	def rslt = []
	def swName  = getSWITCH_NAME(endpoint)
	log("DEBUG", 2, "+++++ createMultipleEvents( endpoint:$endpoint, name:$swName, externalParm:$externalParm, myParm:$myParm)")

	def dni = "${device.deviceNetworkId}-ep${endpoint}"
	def devObj = getChildDevices()?.find { it.deviceNetworkId == dni }
	log("DEBUG", "----- createMultipleEvents: devObj = ${devObj}")
	if (devObj) {
		if (devObj.currentValue("switch") == "$myParm") {
			// log("DEBUG", 2, "<<<<< Child Event unnecessary. name:$dni:$swName evt: \"${myParm}\" ==> dev (${devObj.currentValue("switch")})")
		} else {
			log("DEBUG", 2, "<<<<< Child Event necessary: name:$dni:$swName evt: \"${myParm}\" ==> dev (${devObj.currentValue("switch")})")
			devObj.sendEvent(name: "switch", value: "$myParm", isStateChange: true, displayed: true, descriptionText: "$myParm event sent from parent device")
			rslt << "Note:Event: \"${myParm}\" to child: ${dni}:${devObj}"
		}
	} else {
		log("WARN", "CME: CAN'T FIND CHILD DEVICE: ${dni}:$swName")
	}

	def sw = getSWITCH_NAME(endpoint)
	rslt << createEvent(name: "$sw", value: "$myParm", isStateChange: true, displayed: true, descriptionText: "($sw set to $myParm)")
	rslt
}

def zwaveEvent(hubitat.zwave.Command cmd) {
	log("WARN", "Captured unexpected zwave command $cmd")
	createEvent(descriptionText: "Unexpected command $device.displayName: $cmd", isStateChange: true)
}

//Commands

// Called occasionally although not consistently
def List poll() {
	log("DEBUG", "+++++ poll()")
	executeCommands([], true)
}

private initUILabels() {
	def val = "off"
	sendEvent(name: "M1Name", value: (M1Label ? "${M1Label}" : ""), isStateChange: true, displayed: false, descriptionText: "init M1 Label to ${M1Label}")
	sendEvent(name: "M2Name", value: (M2Label ? "${M2Label}" : ""), isStateChange: true, displayed: false, descriptionText: "init M2 Label to ${M2Label}")
	sendEvent(name: "M3Name", value: (M3Label ? "${M3Label}" : ""), isStateChange: true, displayed: false, descriptionText: "init M3 Label to ${M3Label}")
	sendEvent(name: "M4Name", value: (M4Label ? "${M4Label}" : ""), isStateChange: true, displayed: false, descriptionText: "init M4 Label to ${M4Label}")

	val = VSP_ENABLED ? "off" : "disabled"
	sendEvent(name: "swVSP1", value: val, displayed: false, descriptionText:"")
	sendEvent(name: "swVSP2", value: val, displayed: false, descriptionText:"")
	sendEvent(name: "swVSP3", value: val, displayed: false, descriptionText:"")
	sendEvent(name: "swVSP4", value: val, displayed: false, descriptionText:"")

	val = POOL_SPA_COMBO ? "off" : "disabled"
	sendEvent(name: "poolSpaMode", value: val, displayed: false, descriptionText:"")
}

// Called only by an explicit push of the "Refresh" button
def List refresh() {
	log("DEBUG", "+++++ refresh()  DTH:${VERSION}  state.Versioninfo=${state.VersionInfo}")
	def cmds = []

	// cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1)
	// cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 7)

	cmds << zwave.versionV1.versionGet()
	cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet()

	cmds << zwave.configurationV2.configurationGet(parameterNumber: 1)
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 2)
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 3)
	cmds << zwave.configurationV2.configurationGet(parameterNumber: POOL_SPA_CONFIG)

	if (debugLevel > "5") {
		getSupportedCmdClasses().each {cc ->
			cmds << zwave.versionV1.versionCommandClassGet(requestedCommandClass: cc)
		}
	}

	executeCommands(cmds, true)
}

def List updated() {
	log("DEBUG", "+++++ updated()    DTH:${VERSION}  state.Versioninfo=${state.VersionInfo}  state.ManufacturerInfo=${state.ManufacturerInfo}")
	def devStr = ""
	initUILabels()
	removeOldJunkStateVariables()

	if (getChildDevices() == null) { createChildDevices() }

	// Initialize persistent state variables
	state.lightCircuitsList = getLightCircuits()
	if (state.spaSetpointTemp == null) { state.spaSetpointTemp = 0 }
	if (state.poolSetpointTemp == null) { state.poolSetpointTemp = 0 }
	if (state.scale == null) {state.scale = 1}
	if (state.precision == null) {state.precision = 1}
	if (state.schedules == null) { state.schedules = [] }

	cmds = delayBetweenLog(addRefreshCmds(internalConfigure()))
	cmds.each {key ->
		devStr = devStr.concat("\n<<<<< updated: cmd=$key")
		// sendHubCommand(key)
	}
	sendHubCommand(cmds,0)
	log("DEBUG", "----- updated sent hub commands: " + devStr)
	return []
}

def List configure() {
	log("DEBUG", "+++++ configure()    DTH:${VERSION}  state.Versioninfo=${state.VersionInfo}")
	initUILabels()
	delayBetweenLog(addRefreshCmds(internalConfigure()))
}

private List internalConfigure() {
	def opMode2 = operationMode2.toInteger() & 0x03
	def int tempWater = tempOffsetwater.toInteger()
	def int tempAir   = tempOffsetair.toInteger()
	def cmds = []
	if (tempWater < 0) tempWater = 256 + tempWater
	if (tempAir < 0)   tempAir   = 256 + tempAir

	cmds << zwave.configurationV2.configurationSet(parameterNumber: 1,  size: 2, configurationValue: [operationMode1.toInteger(), opMode2.toInteger()])
	cmds << zwave.configurationV2.configurationSet(parameterNumber: 3,  size: 4, configurationValue: [tempWater, tempAir, 0, 0])
	cmds << zwave.configurationV2.configurationSet(parameterNumber: 2,  size: 1, configurationValue: [fireman.toInteger()])
	cmds << zwave.configurationV2.configurationSet(parameterNumber: POOL_SPA_CONFIG, size: 1, configurationValue: [poolSpa1.toInteger()])

	cmds << zwave.configurationV2.configurationGet(parameterNumber: 1)
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 2)
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 3)
	cmds << zwave.configurationV2.configurationGet(parameterNumber: POOL_SPA_CONFIG)

	logCommandList(cmds)
	cmds
}

/*
 * Child device manipulation
 */

private void createChildDevices() {
	def oldChildren = getChildDevices()
	log("DEBUG", "Existing children: ${oldChildren}")

	for (childNo in 1..5) {
		addOrReuseChildDevice(childNo, "${device.displayName} - Switch ${childNo}", oldChildren)
	}
	if ( POOL_SPA_COMBO ) {
		def childNo = 6
		addOrReuseChildDevice(childNo, "${device.displayName} - Spa Mode", oldChildren)
	}
	if ( VSP_ENABLED ) {
		for (childNo in 7..10) {
			addOrReuseChildDevice(childNo, "${device.displayName} - Speed ${childNo-6}", oldChildren)
		}
	}
	removeChildDevices(oldChildren)
}

private Object addOrReuseChildDevice(childNo, name, List oldChildren){
	def Object devObj = null
	def dni = "${device.deviceNetworkId}-ep${childNo}"
//	log("TRACE", "addOrReuseChildDevice dni=${dni} oldChildren.size=${oldChildren.size} oldChildren: ${oldChildren}")

	devObj = oldChildren.find {it.deviceNetworkId == dni}
	if ( devObj ) {
		// log("TRACE", "found existing device=${devObj.name} dni=${devObj.deviceNetworkId}")
		oldChildren.remove(devObj)
		// log("TRACE", "after remove dni=${dni} oldChildren.size=${oldChildren.size}")
	} else {
		try {
			log("DEBUG", "addChildDevice(namespace=\"erocm123\",DTH Name=\"Switch Child Device\", dni=\"${dni}\", hubId=null,"+
				  "properties=[completedSetup: true, label: \"${name}\","+
				  "isComponent: false, componentName: \"ep${childNo}\", componentLabel: \"Switch ${childNo}\"]")
			// SmartThings
			//			addChildDevice("erocm123", "Switch Child Device", dni, null,
			//            				[completedSetup: true, label: name,
			//                            isComponent: false, componentName: "ep${childNo}", componentLabel: "Switch ${childNo}"])
			//Hubitat
			addChildDevice("erocm123",
					"Switch Child Device",
					dni,
					[isComponent: false, name: "ep${childNo}", label: "Switch ${childNo}"])
		} catch (e) {
			log("DEBUG", "addChildDevice failed: ${e}")
		}
	}
}

private removeChildDevices(List oldChildren){
	log("DEBUG", "RemoveChildDevices(before) count=${oldChildren.size} children: ${oldChildren}")
	try {
		oldChildren.each {child ->
			log("DEBUG", "remove child name=${child.name} displayName=${child.displayName} label=${child.label} dni=${child.deviceNetworkId} id=${child.id}")
			deleteChildDevice(child.deviceNetworkId)
		}
	} catch (e) {
		log("WARN", "Error deleting ${child}, either it didn't exist or probably locked into a SmartApps: ${e}")
	}
}

/*
 * Commands begin here. All return lists of commands, with refresh codes & delays inserted.
 * Related functions (eg, getClock/setClock) are grouped together.
 * Command-generating functions shared by multiple commands are grouped together at the bottom of each section.
 * They do not have delays and refresh codes inserted, to prevent the need for repair later.
 *
 */
/** Clock */
private List getClock() {
	log("DEBUG", "+++++ getClock")
	executeCommands([zwave.clockV1.clockGet()])
}

def List setClock() {
	log("DEBUG",  "+++++ setClock()")
	def nowCal = Calendar.getInstance(location.timeZone)
	timeSet = zwave.clockV1.clockSet(
					hour: nowCal.get(Calendar.HOUR_OF_DAY),
					minute: nowCal.get(Calendar.MINUTE),
					weekday: nowCal.get(Calendar.DAY_OF_WEEK))
	executeCommands([cmds])
}

/** Temperature & Thermostat */
private List setPoolSetpointInternal(Double degrees) {
	log("debug", "+++++ setPoolSetpointInternal() ${degrees}")
	setPoolOrSpaSetpoint(degrees, 1)
}

private List setSpaSetpointInternal(Double degrees) {
	log("debug", "+++++ setSpaSetpointInternal() ${degrees}")
	setPoolOrSpaSetpoint(degrees, 7)
}

private List setPoolOrSpaSetpoint(Double degrees, Integer setpointType) {
	//	Z-Wave standard values for thermostatSetpointSet
	//	precision:		0=no decimal digits, 1=1 decimal digit, 2=2 decimal digits
	//	scale:			0=Celsius, 1=Farenheit
	//	size:			1=8 bit, 2=16 bit, 4=32 bit
	//	setpointType:	1=Heating (pool), 7=Furnace (spa)
	def cmds = []
	def deviceScale = state.scale != null ? state.scale : 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
	def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision
	log("debug", "setPoolSetpointInternal degrees=${degrees} incoming state.scale=${state.scale}  deviceScale=${deviceScale}")

	def convertedDegrees
	//    if (locationScale == "C" && deviceScaleString == "F") {
	//    	convertedDegrees = celsiusToFahrenheit(degrees)
	//    } else if (locationScale == "F" && deviceScaleString == "C") {
	//    	convertedDegrees = fahrenheitToCelsius(degrees)
	//    } else {
	convertedDegrees = degrees
	//    }
	p = 0
	if (setpointType == 1) {
		state.poolSetpointTemp = degrees
	} else if (setpointType == 7) {
		state.spaSetpointTemp = degrees
	}
	//    deviceScale = 0			// Cannot send scale = 1 to v3.4 PE653 or it will ignore the request
	//    deviceScale = deviceScale ? 0 : 1	// Invert deviceScale to be able to test v34.14 firmware
	cmds << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: setpointType, scale: deviceScale, precision: p, size: 1, scaledValue: convertedDegrees)
	cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: setpointType)
	cmds
}

private List getWaterTemp() {
	log("debug", "+++++ getWaterTemp()")
	[zwave.sensorMultilevelV1.sensorMultilevelGet()]
}

/** VSP (Variable-Speed Pump */

// Set all four VSP RPM Speeds and the maximum RPM speed
def List setVSPSpeeds(Integer rpm1, Integer rpm2, Integer rpm3, Integer rpm4, Integer rpmMax) {
	log("DEBUG", "+++++ setVSPSpeeds()  rpm1=${rpm1} rpm2=${rpm2} rpm3=${rpm3} rpm4=${rpm4} rpmMax=${rpmMax}")
	def cmds = []
	[rpm1,rpm2,rpm3,rpm4,rpmMax].eachWithIndex{ spd, inx ->
		// log("DEBUG", "loop spd=$spd  inx=$inx")
		if (inx < 4) {
			cmds.addAll(setConfiguration(getVSP_RPM_SCHED_PARAM(inx+1), 2, spd.intdiv(256), (spd % 256), 0, 0))
		} else {
			cmds.addAll(setConfiguration(getVSP_RPMMAX_SCHED_PARAM(), 2, spd.intdiv(256), (spd % 256), 0, 0))
		}
	}
	executeCommands(cmds, true)
}

// Query the four VSP scheduled to determine which speed is enabled. Not currently used
private List getVSPSpeed() {
	def cmds = []
	log("DEBUG", "+++++ getVSPSpeed()")
	if ( VSP_ENABLED ) {
		for (int sp=1;sp<=4;sp++) {
			cmds.addAll(getChanState(getVSP_CHAN_NO(sp)))
		}
	}
	cmds
}

// Select a VSP speed and request a report to confirm. Not Currently used
private List setVSPSpeedAndGet(Integer speed) {
	log("DEBUG", "+++++ setVSPSpeedAndGet()  speed=${speed}")
	def cmds = []
	cmds.addAll(setVSPSpeedInternal(speed))
	cmds.addAll(getVSPSpeed())
	cmds
}

// Select a VSP speed by forcing the appropriate schedule to always on. speed is from 1-4
// A speed of zero will disable all 4 speeds (off).
// Called based on commands from the Multi-channel SmartApp
private List setVSPSpeedInternal(Integer speed) {
	log("DEBUG", "+++++ setVSPSpeedInternal()  speed=${speed}")
	def cmds = []
	if (speed) {
		cmds.addAll(setChanState(getVSP_CHAN_NO(speed),0xFF))
	} else {
		// Turning off any speed turns off VSP, whether it is the current speed or not
		cmds.addAll(setChanState(getVSP_CHAN_NO(1), 0))
	}
	cmds
}

/** Configuration */
// Send a configuration request
private List getConfiguration(Integer parmNo) {
	log("DEBUG", "+++++ getConfiguration()  parmNo=${parmNo}")
	def cmds = []
	cmds << zwave.configurationV2.configurationGet(parameterNumber: parmNo)
	cmds
}

// Send a configuration command with a variable number of bytes
private List setConfiguration(Integer parmNo, Integer siz, Integer byte0, Integer byte1, Integer byte2, Integer byte3) {
	log("DEBUG", "+++++ setConfiguration()  parmNo=${parmNo} size=${siz}  byte0=${byte0} byte1=${byte1}  byte2=${byte2} byte3=${byte3}")
	def cmds = []
	def ints = [byte0,byte1,byte2,byte3]
	def parmList = []
	ints.eachWithIndex{ rpm, inx ->
		if (inx < siz) {
			parmList << rpm
		}
	}
	// log("DEBUG", " siz=$siz  parmList=$parmList")
	cmds << zwave.configurationV2.configurationSet(configurationValue: parmList, size: siz, parameterNumber: parmNo)
	cmds
}

/** Scheduling */
// Set one schedule based on the schedule number and its start and end time
List setSchedule(Integer endpoint, Integer schedNo, Integer startHour, Integer startMinute, Integer endHour, Integer endMinute) {
	def cmds = []
	def cnfNo = getSCHED_PARAM(endpoint, schedNo)
	log("DEBUG", "+++++ setSchedule()  endpoint=${endpoint} schedNo=${schedNo} cnfNo=${cnfNo} start time=${startHour}:${startMinute}  end time=${endHour}:${endMinute}")
	if (cnfNo) {
		int startTim = startHour * 60 + startMinute
		int endTim = endHour * 60 + endMinute
		cmds.addAll(setConfiguration(cnfNo, 4, (startTim % 256), startTim.intdiv(256), (endTim % 256), endTim.intdiv(256)))
	}
	delayBetweenLog(cmds)
}

// Reset one schedule based on the schedule number
List resetSchedule(Integer endpoint, Integer schedNo) {
	def cmds = []
	def cnfNo = getSCHED_PARAM(endpoint, schedNo)
	log("DEBUG", "+++++ resetSchedule()  endpoint=${endpoint} schedNo=${schedNo} cnfNo=${cnfNo}")
	if (cnfNo) {
		cmds.addAll(setConfiguration(getSCHED_PARAM(endpoint, schedNo), 4, 0xFF, 0xFF, 0xFF, 0xFF))
	}
	delayBetweenLog(cmds)
}

// Request all schedules for an endpoint
List getSchedules(Integer endpoint) {
	def cmds = []
	def cnfNo = getSCHED_PARAM(endpoint, 1)
	log("DEBUG", "+++++ getSchedules()  endpoint=${endpoint} cnfNo=${cnfNo}")
	if (cnfNo) {
		cmds.addAll(getConfiguration(getSCHED_PARAM(endpoint,1)))
		cmds.addAll(getConfiguration(getSCHED_PARAM(endpoint,2)))
		cmds.addAll(getConfiguration(getSCHED_PARAM(endpoint,3)))
	}
	delayBetweenLog(cmds)
}

// General purpose function to set a schedule to "Always off" or "Always on"
private List setSched(int paramNum, Integer val) {
	log("DEBUG", "+++++ setSched(paramNum:${paramNum}, val:$val)")
	def cmds = []
	if (val == 0) {
		cmds << zwave.configurationV2.configurationSet(configurationValue: [0xFF, 0xFF, 0xFF, 0xFF], size: 4, parameterNumber: paramNum)
	} else {
		cmds << zwave.configurationV2.configurationSet(configurationValue: [0x01, 0x00, 0x9F, 0x05], size: 4, parameterNumber: paramNum)
	}
	cmds
}

/** Pool/Spa Mode */
// Not currently used
def List getPoolSpaMode() {
	def cmds = []
	if ( POOL_SPA_COMBO ) {
		cmds = getChanState(POOL_SPA_CHAN)
	}
	cmds
}

private List setPoolSpaMode(Integer val) {
	def cmds = []
	def myValue = ""
	cmds.addAll(setChanState(POOL_SPA_CHAN, val))
	if (val == 0xFF) {
		myValue = "on"
	} else {
		myValue = "off"
	}
	cmds
}

private List setSpaModeInternal() {
	log("DEBUG", "+++++ setSpaModeInternal")
	def cmds = []
	cmds.addAll(setPoolSpaMode(0xFF))
	cmds
}

private List setPoolModeInternal() {
	log("DEBUG", "+++++ setPoolMode")
	def cmds = []
	cmds.addAll(setPoolSpaMode(0))
	cmds
}

private def List togglePoolSpaModeInternal() {
	log("DEBUG", "+++++ togglePoolSpaMode: poolSpaMode:${device.currentValue("poolSpaMode")}")
	def cmds = []
	if (device.currentValue("poolSpaMode").equals("on")) {
		cmds.addAll(setPoolSpaMode(0))
	} else {
		cmds.addAll(setPoolSpaMode(0xFF))
	}
	cmds
}

/** Light Color */
private List setLightColorInternal(int col) {
	log("DEBUG", "+++++ setLightColorInternal ${col}")
	def cmds = []
	sendEvent(name: "lightColor", value: "${col}", isStateChange: true, displayed: true, descriptionText: "Color set to ${col}")
	getColorChgCmds()
}

private List getColorChgCmds() {
	def cmds = []
	int blinkCnt = device.currentValue("lightColor").toInteger()
	if (blinkCnt > 14) blinkCnt = 14;
	if (state.lightCircuitsList) {
		cmds.addAll(blink(state.lightCircuitsList, blinkCnt))
	}
	cmds
}

// Return a list of the Light Circuits selected to have color set
def List getLightCircuits() {
	def lightCircuits = []
	if (C1ColorEnabled == "1") {lightCircuits << 1}
	if (C2ColorEnabled == "1") {lightCircuits << 2}
	if (C3ColorEnabled == "1") {lightCircuits << 3}
	if (C4ColorEnabled == "1") {lightCircuits << 4}
	if (C5ColorEnabled == "1") {lightCircuits << 5}
	// log("TRACE", "lightCircuits=${lightCircuits}  C3ColorEnabled=${C3ColorEnabled}")
	lightCircuits
}

// Alternately turn a switch off then on a fixed number of times. Used to control the color of Pentair pool lights.
private def blink(List switches, int cnt) {
	log("TRACE", "+++++ blink switches=${switches} cnt=${cnt}")
	def cmds = []
	def dly = MIN_DELAY
	for (int i=1; i<=cnt; i++) {
		switches.each { sw ->
			if (cmds) {
				cmds << "delay ${dly}"
			}
			cmds.addAll(setChanState(sw, 0))
			dly = MIN_DELAY
		}
		dly = "${DELAY}"
		switches.each { sw ->
			cmds << "delay ${dly}"
			cmds.addAll(setChanState(sw, 0xFF))
			dly = MIN_DELAY
		}
		dly = "${DELAY}"
	}
	switches.each { sw ->
		cmds << "delay ${dly}"
		cmds.addAll(getChanState(sw))
	}
	//log("TRACE", "blink() cmds=${cmds}")
	cmds
}

/** Shared Refresh commands */
// Called from anywhere that needs the UI controls updated following a Set

def List addRefreshCmds(List cmds)  {
	cmds.addAll(getRefreshCmds())
	cmds
}

private List getRefreshCmds() {
	log("DEBUG", "+++++ getRefreshCmds")
	def cmds =[
		new hubitat.device.HubAction("910005400102870301"),
		new hubitat.device.HubAction("910005400101830101"),
		// new hubitat.device.HubAction("91000541010100"),
	]
	cmds
}

private List refreshCommandStrings() { ["910005400102870301", "910005400101830101"] }
private List refreshCommandHubitatActions() { refreshCommandStrings().collect { new hubitat.device.HubAction(it) } }

private List getTestCmds() {
	log("DEBUG", "+++++ getTestCmds")
	def cmds =[
		// new hubitat.device.HubAction("91000541010100"),
		// zwave.manufacturerProprietaryV1.manufacturerProprietary(payload: "05400101830101")
	]
	// cmds.addAll(setChanState(getVSP_CHAN_NO(2), 0))
	// cmds.addAll(getChanState(getVSP_CHAN_NO(1)))
	// cmds.addAll(setVSPSpeeds(1500,2100,2800,3400,3425) )
	// cmds.addAll(setVSPSpeeds(1400,2000,2750,3450,3450) )
	// cmds.addAll(setSchedule(1,1,0,0,23,59) )
	// cmds.addAll(resetSchedule(1,3) )
	// cmds.addAll(setSchedule(2,1,07,00,19,00) )
	// cmds.addAll(setSchedule(7,1,07,00,18,00) )
	// cmds.addAll(resetSchedule(8,3) )
	cmds.addAll(getSchedules(1) )
	// state.each {key, val ->
	//  log("DEBUG", "state key: $key, value: $val")
	// }
	cmds
}

def on() {
	log("DEBUG", "+++++ on()")
	delayBetweenLog([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.basicV1.basicGet()
	])
}

def off() {
	log("DEBUG", "+++++ off()")
	delayBetweenLog([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.basicV1.basicGet()
	])
}

/** Child (Composite) events protocol */
def List childOn(dni)  {
	log("DEBUG", "childOn called in parent: dni=${dni} channelNumber(dni)=${channelNumber(dni)}")
	this."on${channelNumber(dni)}"()
}

def List childOff(dni)  {
	log("DEBUG", "childOff called in parent: dni=${dni} channelNumber(dni)=${channelNumber(dni)}")
	this."off${channelNumber(dni)}"()
}

def List childRefresh(dni)  {
	log("DEBUG", "refresh called in parent: dni=${dni} channelNumber(dni)=${channelNumber(dni)}")
	delayBetweenLog(addRefreshCmds([]))
}

// On or Off from a child device. Take action depending on which type of child device
private List cmdFromChild(int childNo, int val) {
	def rslt = []
	log("DEBUG", "+++++ cmdFromChild: childNo:$childNo  val:$val")
	log("WARN", "Is this function part of an external protocol? It is not called internally.")

	switch (childNo) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			rslt.addAll(setChanState(childNo, val))
		break
		case POOL_SPA_EP:
			if (val) {
				rslt.addAll(setSpaModeInternal())
			} else {
				rslt.addAll(setPoolModeInternal())
			}
		break
		case getVSP_EP(1):
		case getVSP_EP(2):
		case getVSP_EP(3):
		case getVSP_EP(4):
		// Convert switch endpoint to a VSP speed
			if (val) {
				rslt.addAll(setVSPSpeedInternal( childNo - 6 ))
			} else {
				rslt.addAll(setVSPSpeedInternal( 0 ))
			}
		break
	}
	rslt
}

//Request Switch State
private List getChanState(ch) {
	log("DEBUG", "+++++ getChanState($ch)")
	def cmds =[
		zwave.multiInstanceV1.multiInstanceCmdEncap(instance:ch).encapsulate(zwave.switchBinaryV1.switchBinaryGet())
	]
}

// Set switch instance on/off
private List setChanState(ch, on) {
	log("DEBUG", 2, "+++++ setChanState($ch, $on)")
	def cmds =[
		//zwave.multiInstanceV1.multiInstanceCmdEncap(instance: ch).encapsulate(zwave.switchBinaryV1.switchBinarySet(switchValue: (on ? 0xFF : 0))).format() ,
		multichannelSwitchEvent(ch, on) // Temporarily provide encoded MultiInstanceCommandEncapsulation manually while v1.1.5 of HE firmware has it broken.
	]
}

// Temporarily provide encoded MultiInstanceCommandEncapsulation manually while v1.1.5 of HE firmware has it broken.
// MultiInstanceCmdEncap command class is 6006
// switchBinarySet command is 2501
private String multichannelSwitchEvent(ch, on) {
	def onOff = (on ? "FF" : "00")
	def channelString = ch.toString().padLeft(2, "0")
	"6006" + channelString + "2501" + onOff
}

private List insertLogTrace() {
	cal = Calendar.getInstance(location.timeZone)
	def time = "${String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))}" + ":" +
			"${String.format("%02d", cal.get(Calendar.MINUTE))}" + ":" +
			"${String.format("%02d", cal.get(Calendar.SECOND))}"
	log.info("----------------------- ${time} -----------------------")
	null
}

private List executeArbitraryCommand(String command) {
	log("DEBUG", "+++++ executeArbitraryCommand + ${command}")
	executeCommands([command], true)
}

// Called by switch presses on the circuit buttons.
def List on1()  { executeCommands(setChanState(1, 0xFF), true) }
def List on2()  { executeCommands(setChanState(2, 0xFF), true) }
def List on3()  { executeCommands(setChanState(3, 0xFF), true) }
def List on4()  { executeCommands(setChanState(4, 0xFF), true) }
def List on5()  { executeCommands(setChanState(5, 0xFF), true) }
def List off1() { executeCommands(setChanState(1, 0), true) }
def List off2() { executeCommands(setChanState(2, 0), true) }
def List off3() { executeCommands(setChanState(3, 0), true) }
def List off4() { executeCommands(setChanState(4, 0), true) }
def List off5() { executeCommands(setChanState(5, 0), true) }

// May be called by CoRE
def List setVSPSpeed(sp)       {delayBetweenLog(addRefreshCmds(setVSPSpeedInternal(sp))) }
// Called by switch presses on the VSP buttons.
def List setVSPSpeed0()        {delayBetweenLog(addRefreshCmds(setVSPSpeedInternal(0))) }
def List setVSPSpeed1()        {delayBetweenLog(addRefreshCmds(setVSPSpeedInternal(1))) }
def List setVSPSpeed2()        {delayBetweenLog(addRefreshCmds(setVSPSpeedInternal(2))) }
def List setVSPSpeed3()        {delayBetweenLog(addRefreshCmds(setVSPSpeedInternal(3))) }
def List setVSPSpeed4()        {delayBetweenLog(addRefreshCmds(setVSPSpeedInternal(4))) }

def List setSpaMode()          { executeCommands(setSpaModeInternal()) }
def List setPoolMode()         { executeCommands(setPoolModeInternal()) }
def List togglePoolSpaMode()   { executeCommands(togglePoolSpaModeInternal()) }

def List quickSetSpa(degrees)  { executeCommands(setSpaSetpointInternal("${degrees}".toDouble()), true) }
def List quickSetPool(degrees) { executeCommands(setPoolSetpointInternal("${degrees}".toDouble()), true) }

def List quickGetWaterTemp()   {
	log("DEBUG", "+++++ quickGetWaterTemp")
	executeCommands(getWaterTemp(), true)
}
def List quickGetWaterTempOld() {
	log("DEBUG", "+++++ quickGetwaterTempOld using delayBetweenLog")
	delayBetweenLog( addRefreshCmds(getWaterTemp()))
}

def List quickGetTestCmds()    { executeCommands(getTestCmds(), true) }
def List setLightColor(int col){ executeCommands(setLightColorInternal(col), true) }
//def List setColor()			   {addRefreshCmds(delayBetweenLog(cmds)) } // This seems poorly defined currently

// Called from Parse for responses from the device
def delayResponseLog(parm, dly=DELAY, responseFlg=true) {
	delayBetweenLog(parm, dly, responseFlg)
}


// Called from all commands
def delayBetweenLog(parm, dly=DELAY, responseFlg=false) {
	log("DEBUG", "+++++ delayBetweenLog parm[${parm.size}] dly=$dly responseFlg=${responseFlg}")
	def lst = parm
	def cmds =[]
	def evts =[]
	def devStr = ""
	def evtStr = ""
	def fmt = ""
	if (!(parm in List)) {
		lst = [parm]
	}
	lst.eachWithIndex {l, index ->
		if (l instanceof List) {
			log("WARN", "  - UNEXPECTED instanceOf List: l -> ${l}")
			log("ERROR", "I suspect this never occurs - l instanceof List case.")
		} else if (l in List) {
			log("WARN", "  - UNEXPECTED in LIST: l -> ${l}")
			log("ERROR", "I suspect this never occurs - l in List case.")
		} else {
			log("TRACE", "  - l -> ${l}") // maps come through here
		}

		if (l instanceof hubitat.device.HubAction) {
			log("TRACE", "  - ${index}: instanceof hubitat.device.HubAction")
			log("ERROR", "I suspect this never occurs. - HubAction case")
			if (cmds) {
				def c = cmds.last()			//check if there is already a delay prior to this
				if (!(c instanceof String || c instanceof GString) || c.take(6) != "delay ") {
					cmds << "delay $dly"
					devStr = devStr.concat(", delay $dly")
				}
			}
			if (responseFlg) { // Keith's suggestion to handle the various types
				cmds << l
			} else {
				cmds << l.toString()
			}
			devStr = devStr.concat("\n\t\t\t<<<<< HubAction: $l")
		} else if (l instanceof String || l instanceof GString) {
			log("TRACE", "  - ${index}: String: $l")
			if (l.take(5) == "Note:") {
				evtStr = evtStr.concat("\n<<<<< ${l.drop(5)}")
			} else {
				cmds << l
				devStr = devStr.concat(", ${l}")
			}
		} else if (l instanceof List) {
			log("TRACE", "  - ${index}: LIST: $l, adding commands")
			log("ERROR", "Encountered list within list. I suspect this never occurs.")
			cmds << l
		} else if (l instanceof Map) { // All responses from device come in as maps.
			log("TRACE", "  - ${index}: Map: $l")
			// example:	createEvent(name: "$sw", value: "$myParm", isStateChange: true, displayed: true, descriptionText: "($sw set to $myParm)")
			if ("${device.currentValue(l.name)}".equals("${l.value}")) {
				log("DEBUG", 2, "\t\t Event unnecessary. name:${l.name}  evt: \"${l.value}\" ==> dev:(${device.currentValue(l.name)})")
			} else {
				log("DEBUG", "\t\t Event necessary. name:${l.name} evt: \"${l.value}\" ==> dev:(${device.currentValue(l.name)})")
				evts << l
				evtStr = evtStr.concat("\n\t\t\t<<<<< Event: $l")
			}
		} else {
			if (responseFlg) {
				fmt = response(l)
			} else {
				fmt = l.format()
			}
			log("TRACE", "  - ${index}: else: HubAction: $l, format()=$fmt")

			if (cmds) {
				def c = cmds.last()			//check if there is already a delay prior to this
				if (!(c instanceof String || c instanceof GString) || c.take(6) != "delay ") {
					cmds << "delay $dly"
					devStr = devStr.concat(", delay $dly")
				}
			}
			devStr = devStr.concat("\n\t\t\t Dev cmd: $l  --> $fmt")
			cmds << fmt
		}
	}
	evts.addAll(cmds)

	if (evts) {
		log("DEBUG", "<<<<< rspFlg=${responseFlg} dly:$dly/${DELAY}${evtStr}${devStr}")
		def eventsList = ""
		evts.eachWithIndex { event, index ->
			eventsList = eventsList.concat("\t\t\t\t${index}: ${event}\n")
		}
		// log("DEBUG", 2, "  - Events as sent: \n${eventsList}")
		logCommandList(evts)
		evts
	} else {
		log("WARN", 2, "<<<<< rspFlg=${responseFlg} dly:$dly/${DELAY} No Commands or Events")
		null
	}
}

// Utility functions
private channelNumber(String dni) {
	log("DEBUG", "channelNumber:  dni:$dni   [-1] ${dni.split("-ep")[-1]}   [0] ${dni.split("-ep")[0]}   [1] ${dni.split("-ep")[1]}")
	dni.split("-ep")[1] as Integer
}

private List executeCommands(commands, refreshControls = false) {
	log("DEBUG", 2, "+++++ executeCommands")
	logCommandList(commands)

	if (refreshControls) {
		commands += refreshCommandStrings()
	}

	commands = formatUnformattedCommands(commands)

	log("DEBUG", 2, "----- executeCommands final set:")
	def afterDelayBetween = delayBetween(commands, 1000)
	logCommandList(afterDelayBetween)
	afterDelayBetween
}

private List formatUnformattedCommands(commands) {
	formattedCommands = []
	commands.each { command ->
		if (command instanceof String || command instanceof GString) {
			formattedCommands << command
		} else {
			formattedCommands << command.format()
		}
	}
	formattedCommands
}

private log(String type, Integer level = 1, String message) {
	if (level == 0) { return }
	if ( ! (debugLevel.any { element -> (element as int) >= (level as int)})) { return }
	if ( ! ["ERROR", "WARN", "INFO", "DEBUG", "TRACE"].contains(type.toUpperCase())) { return }

	def logType = type.toLowerCase()
	log."$logType"(message)
}

private logCommandList(commands) {
	def eventsList = ""
	def formattedCommand = ""
	commands.eachWithIndex { cmd, index ->
		formattedCommand = "\t" +
						index.toString().padRight(3) +
						"[ " +
						cmd.toString().padRight(20) +
						"] - "

		if (! (cmd instanceof String || cmd instanceof GString || cmd instanceof hubitat.device.HubAction || cmd instanceof java.util.LinkedHashMap)) {
			formattedCommand += cmd.format().padRight(16)
		} else {
			formattedCommand += "String Command".padRight(16)
		}
		eventsList = eventsList.concat("\t${formattedCommand}\n")
	}
	log("DEBUG", 2, " Commands: \n${eventsList}")
}

private removeOldJunkStateVariables() {
	// Clean up old junk state variables
	state.remove("groups")
	state.remove("cnfData")
	state.remove("cnfData2")
	state.remove("cnfAttemptsLeft")
	state.remove("cnfGetGoal")
	state.remove("cnfParallelGets")
	state.remove("cnfSendParmOne")
	state.remove("endpoints")
	state.remove("enabledEndpoints")
	state.remove("endpointInfo")
	state.remove("nextParm")
	state.remove("manufacturer")
	state.remove("pumpSpeed")
	state.remove("oldLabel")
	state.remove("children")
}
