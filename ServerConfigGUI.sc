ServerConfigGUI {
	classvar win, msgBox, outDevice, sampleRate;

	*start {arg winWidth = 500;

		var sampleRates = Dictionary.newFrom(["44.1kHz", 44100, "48kHz", 48000, "96kHz", 96000, "192kHz", 192000]);
		var outMenu, srMenu, msgText, bootButton, killButton;
		win = Window.new("Select Device & Boot", Rect(200, 200, winWidth, 350), false);

		outMenu = PopUpMenu(win, Rect(10, 10, 430, 30)).resize_(2).font_(this.prDefaultFont(10, false));


		StaticText.new(win, Rect(10, 15, 100, 200)).font_(this.prDefaultFont(10, false)).string_("Sample Rate");
		srMenu = PopUpMenu(win, Rect(10, 125, 100, 30)).font_(this.prDefaultFont(10, true));

		msgBox = CompositeView.new(win, Rect(10, 180, 480, 150)).background_(Color.fromHexString("#a7a7a7"));
		msgText = StaticText.new(msgBox, Rect(10, -75, 425, 200)).font_(this.prDefaultFont(14, true)).stringColor_(Color.red);

		bootButton = Button(win, Rect(10,60, 80, 30)).states_([["Boot", Color.black,Color.cyan],["Reboot",Color.blue,Color.white]]).font_(this.prDefaultFont(12, true));

		killButton = Button(win, Rect(110,60,120,30)).states_([["Kill Switch", Color.black, Color.red]]).font_(this.prDefaultFont(12, true));

		outMenu.items = ServerOptions.outDevices;
		srMenu.items = sampleRates.keys.asArray;

		bootButton.action = {
			outDevice = outMenu.item.asString;
			sampleRate = sampleRates.at(srMenu.item);
			try{this.prStartServer;}
			{msgText.string_("Error starting the server.");};
			msgText.string_("Device Set to: " + outMenu.item);
		};
		killButton.action = {Server.default.ifRunning({
			Server.killAll; killButton.value = 0; bootButton.value = 0; msgText.visible = false;

		}, {msgText.string_("Server Not Running");});};
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
			s.options.numBuffers = 1024 * 256;
			s.options.memSize = 8192 * 32;
			s.options.numWireBufs = 64;
			s.options.maxNodes = 1024 * 64;
			s.options.numOutputBusChannels = 2;
			s.options.numInputBusChannels = 2;
			s.options.outDevice = outDevice;
			s.options.sampleRate = sampleRate;
			s.latency = 0.3;


		}, {Exception("There was an issue starting the server");});
	}

}