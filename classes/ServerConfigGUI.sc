ServerConfigGUI {
	classvar win, settings, path, defaultSettings, usingLinux;

	*start {
        this.prDraw;
    }

    *prDraw{arg winWidth = 500;
		var sampleRates = Dictionary.newFrom(["44.1kHz", 44100, "48kHz", 48000, "96kHz", 96000, "192kHz", 192000]);
		var inMenu, outMenu, srMenu, bootButton, killButton, numBuffEntry, wireBuffEntry, nodeEntry, latencyEntry, saveButton, updateSettings, resetButton, serverMemoryEntry, dropDownSelector, inList, outList;
		Platform.case(
			\linux, {usingLinux = true},
			\windows, {usingLinux = false},
			\osx, {usingLinux = false}
		);
        path = Platform.userExtensionDir +/+ "server-settings.save";

        defaultSettings = Dictionary.newFrom(["InDevice", "Default", "OutDevice", "Default", "SampleRate", 44100,
            "SampleBuffers", 1024, "ServerMemory", 8192, "WireBuffers", 64,
            "ServerNodes", 1024, "InputBuffers", 2, "OutputBuffers", 2, "ServerLatency", 0.2]);

        settings = defaultSettings;



        // overwrite default settings if save exists
        if(File.exists(path), {
            var temp = File.readAllString(path).split($;);
            temp.removeAt(temp.size - 1);
            if(temp.size == 10, {
                settings = Dictionary();
                temp.do({ |stringPair|
                    var pair = stringPair.split($,);
                    switch(pair[0],
                        "InDevice", {settings.add(pair[0] -> pair[1])},
                        "OutDevice", {settings.add(pair[0] -> pair[1])},
                        "ServerLatency", {settings.add(pair[0] -> pair[1].asFloat)},
                        {settings.add(pair[0] -> pair[1].asInteger)}
                    );
                });
            });
        });


		win = Window.new("Select Device & Boot", Rect(200, 200, winWidth, 450), false);

		if(usingLinux, {
			StaticText(win, Rect(10, 10, 600, 80)).font_(this.prDefaultFont(16, false))
			.string_("Audio devices on Linux are configured via JACK.");
			settings.add("OutDevice" -> "JACK");
			settings.add("InDevice" -> "JACK");
		},{
			inList = ServerOptions.inDevices;
			inList = inList.insert(0, "Default");
			outList = ServerOptions.outDevices;
			outList = outList.insert(0, "Default");

			StaticText(win, Rect(10, 10, 100, 20)).font_(this.prDefaultFont(10, false)).string_("Input Device");
			inMenu = PopUpMenu(win,  Rect(10, 30, 430, 30)).resize_(2).font_(this.prDefaultFont(10, false)).items_(inList);
			if(settings["InDevice"] == "Default", {inMenu.value = 0}, {
				var index = inMenu.items.detectIndex({arg item; item == settings["InDevice"]});
				if(index == nil, {inMenu.value = 0}, {inMenu.value = index});
			});


			StaticText(win, Rect(10, 60, 100, 20)).font_(this.prDefaultFont(10, false)).string_("Output Device");
			outMenu = PopUpMenu(win, Rect(10, 80, 430, 30)).resize_(2).font_(this.prDefaultFont(10, false)).items_(outList);
			if(settings["OutDevice"] == "Default", {outMenu.value = 0}, {
				var index = outMenu.items.detectIndex({arg item; item == settings["OutDevice"]});
				if(index == nil, {outMenu.value = 0}, {outMenu.value = index});
			});
		}
		);




		StaticText.new(win, Rect(10, 165, 100, 20)).font_(this.prDefaultFont(10, false)).string_("Sample Rate");
		if(usingLinux, {StaticText.new(win, Rect(10, 185, 125, 30)).font_(this.prDefaultFont(8, true)).string_("Managed by JACK")},

		{srMenu = PopUpMenu(win, Rect(10, 185, 100, 30)).font_(this.prDefaultFont(10, true)).items_(sampleRates.keys.asArray);
		srMenu.value = srMenu.items.detectIndex({arg item; item == sampleRates.findKeyForValue(settings["SampleRate"]);});}
		);

        StaticText.new(win, Rect(150, 165, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Sample Buffers");
        numBuffEntry = NumberBox(win, Rect(150, 185, 100, 30)).value_(settings["SampleBuffers"]).clipLo_(1024).font_(this.prDefaultFont(10)).step_(0).scroll_step_(0);

        StaticText(win, Rect(10, 220, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Server Memory");
        serverMemoryEntry = NumberBox(win, Rect(10, 240, 100, 30)).font_(this.prDefaultFont(10)).clipLo_(8192).step_(0).scroll_step_(0);
        serverMemoryEntry.value = settings["ServerMemory"];

        StaticText.new(win, Rect(150, 220, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Wire Buffers");
        wireBuffEntry = NumberBox(win, Rect(150, 240, 100, 30)).font_(this.prDefaultFont(10)).value_(settings["WireBuffers"]).clipLo_(64).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(10, 275, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Nodes");
		nodeEntry = NumberBox.new(win, Rect(10, 295, 100, 30)).font_(this.prDefaultFont(10)).value_(settings["ServerNodes"]).clipLo_(1024).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(10, 330, 150, 20)).font_(this.prDefaultFont(10, false)).string_("Server Latency");
		latencyEntry = NumberBox.new(win, Rect(10, 350, 100, 30)).font_(this.prDefaultFont(10)).value_(settings["ServerLatency"]).step_(0).scroll_step_(0);

        saveButton = Button.new(win, Rect(150, 350, 120, 30)).font_(this.prDefaultFont(10)).states_([["Save Settings", Color.black, Color.fromHexString("#34FA97")]]);

        resetButton = Button.new(win, Rect(150, 400, 120, 45)).states_([["Reset to \ndefault settings", Color.black, Color.gray]]).font_(this.prDefaultFont(10));

		bootButton = Button(win, Rect(10, 120, 80, 30)).states_([["Boot", Color.black,Color.cyan],["Reboot",Color.blue,Color.white]]).font_(this.prDefaultFont);

        killButton = Button(win, Rect(110, 120, 120, 30)).states_([["Kill Switch", Color.black, Color.red]]).font_(this.prDefaultFont);

        updateSettings = {
            settings.add("SampleBuffers" -> numBuffEntry.value);
            settings.add("ServerMemory" -> serverMemoryEntry.value);
            settings.add("WireBuffers" -> wireBuffEntry.value);
            settings.add("ServerNodes"-> nodeEntry.value);
            settings.add("ServerLatency" -> latencyEntry.value);
			if(usingLinux != true, {
				settings.add("SampleRate"-> sampleRates.at(srMenu.item));
				settings.add("OutDevice" -> outMenu.item.asString);
				settings.add("InDevice" -> inMenu.item.asString);
			});
        };


		bootButton.action = {
            updateSettings.value;
            this.prStartServer();
            win.name_("Out: " + settings["OutDevice"]);

		};

		killButton.action = {
			Server.killAll; killButton.value = 0; bootButton.value = 0;
		};

        saveButton.action = {
            var f;
            updateSettings.value;
            f = File.open(path, "w");
            settings.keysValuesDo({|key, value|
            f.write(key ++ "," ++ value ++ ";")});
            f.close;
            this.prMessageBox("Settings Saved");
        };

        resetButton.action = {
            var f;
            settings = defaultSettings;
            f = File.open(path, "w");
            settings.keysValuesDo({|key, value|
                f.write(key ++ "," ++ value ++ ";")
            });
            f.close;

            wireBuffEntry.value = settings["WireBuffers"];
            serverMemoryEntry.value = settings["WireBuffers"];
            nodeEntry.value = settings["ServerNodes"];
            latencyEntry.value = settings["ServerLatency"];
			if(usingLinux != true, {
				outMenu.value = 0;
				srMenu.value = 0;
			});
            this.prMessageBox("Settings Reset");

        };

		win.front;

    }

	*prDefaultFont {arg size = 12, em = true;
		^Font.new(Font.defaultSansFace, size, em, usePointSize: true);
	}

	*prStartServer {
        var o = ServerOptions.new;
        var s = Server.default;
        if(s.serverRunning, {Server.killAll});
        o.numBuffers_(settings["SampleBuffers"]);
        o.memSize_(settings["ServerMemory"]);
        o.numWireBufs_(settings["WireBuffers"]);
        o.maxNodes_(settings["ServerNodes"]);
        o.sampleRate_(settings["SampleRate"]);
		if(usingLinux != true, {
			if(settings["InDevice"] != "Default" && settings["OutDevice"] != "Default",
            {o.inDevice_(settings["InDevice"]); o.outDevice_(settings["OutDevice"]);});
		});
        s.options_(o);
        s.boot;
        s.latency_(settings["ServerLatency"]);

	}

	*prMessageBox { arg msg;
		var box = win.bounds;
		var w = Window.new("Warning", Rect(box.left + (box.width / 2) - 225, box.top + (box.height / 2), 450, 100), false).alwaysOnTop_(true);
		StaticText.new(w, Rect(10, 10, 430, 80)).string_(msg).font_(this.prDefaultFont(26));
		w.front();

	}

}
