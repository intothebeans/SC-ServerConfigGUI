ServerConfigGUI {
	classvar win, settings, path, defaultSettings;

	*start {arg winWidth = 500;

		var sampleRates = Dictionary.newFrom(["44.1kHz", 44100, "48kHz", 48000, "96kHz", 96000, "192kHz", 192000]);
		var outMenu, srMenu, bootButton, killButton, numBuffEntry, memoryEntry,
        wireBuffEntry, nodeEntry, inBuffEntry, outBuffEntry, latencyEntry, saveButton, updateSettings, saveSettings, resetButton;

        path = Platform.userConfigDir +/+ "server-settings.save";

        defaultSettings = Dictionary.newFrom(["OutDevice", ServerOptions.outDevices[0], "SampleRate", 44100,
            "SampleBuffers", 1024, "ServerMemory", 8192, "WireBuffers", 64,
            "ServerNodes", 1024, "InputBuffers", 2, "OutputBuffers", 2, "ServerLatency", 0.2]);

        settings = defaultSettings;

        // overwrite default settings if save exists
        if(File.exists(path), {
            var temp = File.readAllString(path).split($;);
            temp.removeAt(temp.size - 1);
            temp.postln;
            if(temp.notEmpty, {
                settings = Dictionary();
                temp.do({ |stringPair|
                    var pair = stringPair.split($,);
                    if(pair[0] != "OutDevice", {
                        settings.add(pair[0] -> pair[1].asInteger);
                    }, {
                        if(pair[0] == "ServerLatency", {settings.add(pair[0] -> pair[1].asFloat)}, {
                            settings.add(pair[0] -> pair[1]);
                        });
                    });
                });
            });
        });

		win = Window.new("Select Device & Boot", Rect(200, 200, winWidth, 400), false);

		outMenu = PopUpMenu(win, Rect(10, 10, 430, 30)).resize_(2).font_(this.prDefaultFont(10, false));
		outMenu.items = ServerOptions.outDevices;

		StaticText.new(win, Rect(10, 105, 100, 20)).font_(this.prDefaultFont(10, false)).string_("Sample Rate");
		srMenu = PopUpMenu(win, Rect(10, 125, 100, 30)).font_(this.prDefaultFont(10, true)).items_(sampleRates.keys.asArray);
  /*      tempArray.detectIndex{
            arg item;
            item == sampleRates.findKeyForValue(settings[\SampleRate]);
        };*/


        StaticText.new(win, Rect(150, 105, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Sample Buffers");
        numBuffEntry = NumberBox.new(win, Rect(150, 125, 100, 30))
        .value_(settings["SampleBuffers"]).clipLo_(1024).font_(this.prDefaultFont(10)).step_(0).scroll_step_(0);

        StaticText.new(win, Rect(10, 160, 150, 20)).font_(this.prDefaultFont(10, False)).string_("Server Memory");
        memoryEntry = NumberBox.new(win, Rect(10, 180, 100, 30))
        .font_(this.prDefaultFont(10)).value_(settings["ServerMemory"]).clipLo_(8192).step_(0).scroll_step(0);

        StaticText.new(win, Rect(150, 160, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Wire Buffers");
        wireBuffEntry = NumberBox.new(win, Rect(150, 180, 100, 30))
        .font_(this.prDefaultFont(10)).value_(settings["WireBuffers"]).clipLo_(64).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(10, 215, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Nodes");
		nodeEntry = NumberBox.new(win, Rect(10, 235, 100, 30))
        .font_(this.prDefaultFont(10)).value_(settings["ServerNodes"]).clipLo_(1024).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(150, 215, 150, 20)).font_(this.prDefaultFont(10, false)).string_("In/Out Buffers");
		inBuffEntry = NumberBox.new(win, Rect(150, 235, 45, 30))
        .font_(this.prDefaultFont(10)).value_(settings["InputBuffers"]).step_(0).scroll_step_(0);
		outBuffEntry = NumberBox.new(win, Rect(205, 235, 45, 30))
        .font_(this.prDefaultFont(10)).value_(settings["OutputBuffers"]).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(10, 270, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Server Latency");
		latencyEntry = NumberBox.new(win, Rect(10, 290, 100, 30))
        .font_(this.prDefaultFont(10)).value_(settings["ServerLatency"]).step_(0).scroll_step_(0);

        saveButton = Button.new(win, Rect(150, 290, 120, 30))
        .font_(this.prDefaultFont(10)).states_([["Save Settings", Color.black, Color.fromHexString("#34FA97")]]);

        resetButton = Button.new(win, Rect(150, 340, 120, 45)).states_([["Reset to \ndefault settings", Color.black, Color.gray]]).font_(this.prDefaultFont(10));

		bootButton = Button(win, Rect(10, 60, 80, 30))
        .states_([["Boot", Color.black,Color.cyan],["Reboot",Color.blue,Color.white]]).font_(this.prDefaultFont);

        killButton = Button(win, Rect(110,60,120,30)).states_([["Kill Switch", Color.black, Color.red]]).font_(this.prDefaultFont);

        updateSettings = {
            settings.add("OutDevice" -> outMenu.item.asString);
            settings.add("SampleRate"-> sampleRates.at(srMenu.item));
            settings.add("SampleBuffers" -> numBuffEntry.value);
            settings.add("ServerMemory" -> memoryEntry.value);
            settings.add("WireBuffers" -> wireBuffEntry.value);
            settings.add("ServerNodes"-> nodeEntry.value);
            settings.add("InputBuffers" -> inBuffEntry.value);
            settings.add("OutputBuffers"-> outBuffEntry.value);
            settings.add("ServerLatency" -> latencyEntry.value);

        };

        saveSettings = { |update = true|
            var file;
            if(update, {updateSettings.value});
            file = File.open(path, "w");
            if(file.isOpen, {
                settings.keysValuesDo({|key, value|
                file.write(key ++ "," ++ value ++ ";");
            });
                file.close;
                this.prMessageBox("Settings Saved");
            }, {
                this.prMessageBox("Error Saving File");
            });

        };

		bootButton.action = {
            updateSettings.value;
			try{this.prStartServer;}
			{this.prMessageBox("Error starting the server.");};
			win.name_("Device: " + settings[\OutDevice]);
		};

		killButton.action = {Server.default.ifRunning({
			Server.killAll; killButton.value = 0; bootButton.value = 0;

		}, {this.prMessageBox("Server Isn't Running");});};

        saveButton.action = saveSettings;

        resetButton.action = {
            settings = defaultSettings;
            saveSettings.value(false);

            numBuffEntry.value_(settings["SampleBuffers"]);
            memoryEntry.value_(settings["ServerMemory"]);
            wireBuffEntry.value_(settings["WireBuffers"]);
            nodeEntry.value_(settings["ServerNodes"]);
            inBuffEntry.value_(settings["InputBuffers"]);
            outBuffEntry.value_(settings["OutputBuffers"]);
            latencyEntry.value_(settings["ServerLatency"]);


        };

		win.front;

    }

	*prDefaultFont {arg size = 12, em = true;
		^Font.new(Font.defaultSansFace, size, em, usePointSize: true);
	}

	*prStartServer {
		var s = Server.default;
		if(s.serverRunning, {Server.killAll;});
		s.reboot( {
			// see http://doc.sccode.org/Classes/ServerOptions.html
            s.options.numBuffers = settings["SampleBuffers"]; //256
            s.options.memSize = settings["SystemMemory"]; //32
            s.options.numWireBufs = settings["WireBuffers"];
            s.options.maxNodes = settings["ServerNodes"]; //64
            s.options.numOutputBusChannels = settings["OutputBuffs"];
            s.options.numInputBusChannels = settings["InputBuffs"];
            s.options.outDevice = settings["OutDevice"];
            s.options.sampleRate = settings["SampleRate"];
            s.latency = settings["ServerLatency"];


		}, {Exception("There was an issue starting the server");});
	}

	*prMessageBox { arg msg;
		var box = win.bounds;
		var w = Window.new("Warning", Rect(box.left + (box.width / 2) - 225, box.top + (box.height / 2), 450, 100), false).alwaysOnTop_(true);
		StaticText.new(w, Rect(10, 10, 430, 80)).string_(msg).font_(this.prDefaultFont(26));
		w.front();

	}

}