ServerConfigGUI {
	classvar win, msgText, outDevice, sampleRate, buffs, memory, wireBuffs, nodes, inBuffs, outBuffs, latency;

	*start {arg winWidth = 500, boxWidth = 480;

		var sampleRates = Dictionary.newFrom(["44.1kHz", 44100, "48kHz", 48000, "96kHz", 96000, "192kHz", 192000]);
		var outMenu, srMenu, msgBox, bootButton, killButton, numBuffEntry, memoryEntry, wireBuffEntry, nodeEntry, inBuffEntry, outBuffEntry, latencyEntry, saveButton;
		win = Window.new("Select Device & Boot", Rect(200, 200, winWidth, 350), false);

		outMenu = PopUpMenu(win, Rect(10, 10, 430, 30)).resize_(2).font_(this.prDefaultFont(10, false));


        // I know the EZ gui items exist, i just prefer this way
		StaticText.new(win, Rect(10, 15, 100, 200)).font_(this.prDefaultFont(10, false)).string_("Sample Rate");
		srMenu = PopUpMenu(win, Rect(10, 125, 100, 30)).font_(this.prDefaultFont(10, true));

        StaticText.new(win, Rect(150, 15, 150, 200)).font_(this.prDefaultFont(10, false)).string_("Sample Buffers");
        numBuffEntry = NumberBox.new(win, Rect(150, 125, 100, 30))
        .value_(1024).clipLo_(1024).font_(this.prDefaultFont(10)).step_(0).scroll_step_(0);

        StaticText.new(win, Rect(10, 70, 150, 200)).font_(this.prDefaultFont(10, False)).string_("Server Memory");
        memoryEntry = NumberBox.new(win, Rect(10, 180, 100, 30))
        .font_(this.prDefaultFont(10)).value_(8192).clipLo_(8192).step_(0).scroll_step(0);

        StaticText.new(win, Rect(150, 70, 150, 200)).font_(this.prDefaultFont(10, false)).string_("Wire Buffers");
        wireBuffEntry = NumberBox.new(win, Rect(150, 180, 100, 30))
        .font_(this.prDefaultFont(10)).value_(64).clipLo_(64).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(10, 125, 150, 200)).font_(this.prDefaultFont(10, false)).string_("Nodes");
		nodeEntry = NumberBox.new(win, Rect(10, 235, 100, 30))
		.font_(this.prDefaultFont(10)).value_(1024).clipLo_(1024).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(150, 125, 150, 200)).font_(this.prDefaultFont(10, false)).string_("In/Out Buffers");
		inBuffEntry = NumberBox.new(win, Rect(150, 235, 45, 30)).font_(this.prDefaultFont(10)).value_(2).step_(0).scroll_step_(0);
		outBuffEntry = NumberBox.new(win, Rect(205, 235, 45, 30)).font_(this.prDefaultFont(10)).value_(2).step_(0).scroll_step_(0);

		StaticText.new(win, Rect(10, 180, 150, 200)).font_(this.prDefaultFont(10, false)).string_("Server Latency");
		latencyEntry = NumberBox.new(win, Rect(10, 290, 100, 30)).font_(this.prDefaultFont(10)).value_(0.2).step_(0).scroll_step_(0);

        saveButton = Button.new(win, Rect(150, 180, 150, 200))
        .font_(this.prDefaultFont).states_([["Save Settings", Color.black, Color.fromHexString("#34FA97")]]);

		bootButton = Button(win, Rect(10, 60, 80, 30))
        .states_([["Boot", Color.black,Color.cyan],["Reboot",Color.blue,Color.white]]).font_(this.prDefaultFont(12, true));

		killButton = Button(win, Rect(110,60,120,30)).states_([["Kill Switch", Color.black, Color.red]]).font_(this.prDefaultFont(12, true));

		outMenu.items = ServerOptions.outDevices;
		srMenu.items = sampleRates.keys.asArray;

		bootButton.action = {
			outDevice = outMenu.item.asString;
			sampleRate = sampleRates.at(srMenu.item);
            buffs = numBuffEntry.value;
            memory = memoryEntry.value;
			wireBuffs = wireBuffEntry.value;
			nodes = nodeEntry.value;
			inBuffs = inBuffEntry.value;
			outBuffs = outBuffEntry.value;
			latency = latencyEntry.value;
			try{this.prStartServer;}
			{this.prMessageBox("Error starting the server.");};
			win.name_("Device: " + outDevice);
		};
		killButton.action = {Server.default.ifRunning({
			Server.killAll; killButton.value = 0; bootButton.value = 0;

		}, {this.prMessageBox("Server Isn't Running");});};
		win.front;
	}

	*prDefaultFont {arg size = 12, em = true;
		^Font.new(Font.defaultMonoFace, size, em, usePointSize: true);
	}

	*prStartServer {
		var s = Server.default;
		if(s.serverRunning, {Server.killAll;});
		s.reboot( {
			// see http://doc.sccode.org/Classes/ServerOptions.html
			s.options.numBuffers = buffs; //256
			s.options.memSize = memory; //32
			s.options.numWireBufs = wireBuffs;
			s.options.maxNodes = nodes; //64
			s.options.numOutputBusChannels = outBuffs;
			s.options.numInputBusChannels = inBuffs;
			s.options.outDevice = outDevice;
			s.options.sampleRate = sampleRate;
			s.latency = latency;


		}, {Exception("There was an issue starting the server");});
	}

	*prMessageBox { arg msg;
		var box = win.bounds;
		var w = Window.new("Warning", Rect(box.left + (box.width / 2) - 225, box.top + (box.height / 2), 450, 100), false).alwaysOnTop_(true);
		StaticText.new(w, Rect(10, 10, 430, 80)).string_(msg).font_(this.prDefaultFont(26));
		w.front();

	}

}