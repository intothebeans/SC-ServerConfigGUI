ServerConfigGUI {
	classvar win, msgBox, outDevice, sampleRate;

	*start{arg winWidth = 100;
		var sampleRates = Dictionary.newFrom(["44.1kHz", 44100, "48kHz", 48000, "96kHz", 96000, "192kHz", 192000]);
		win = Window.new("Select Device & Boot", Rect(200, 200, winWidth, 350),false);

		// create a drop down menu with all the output devices
		var outMenu = PopUpMenu(win, Rect(10, 10, 430, 30)).resize_(2).font_(this.prDefaultFont(10, false));

		// drop down for changing the sample rate
		var label1 = StaticText.new(win, Rect(10, 15, 100, 200)).font_(this.prDefaultFont(10, false));
		var srMenu = PopUpMenu(win, Rect(10, 125, 100, 30)).font_(this.prDefaultFont(10, true));

		// create a box to toggle visibility on button clicks for alerts
		msgBox = CompositeView.new(win, Rect(10, 140, 400,300));
		var msgText = StaticText.new(msgBox, Rect(0,0, 425, 200)).font_(this.prDefaultFont(14, true)).stringColor_(Color.red);;

		// draw buttons
		var b1 = Button(win, Rect(10,60, 80, 30)).states_([["Boot", Color.black,Color.cyan],["Reboot",Color.blue,Color.white]]).font_(this.prDefaultFont(12, true));

		var b2 = Button(win, Rect(110,60,120,30)).states_([["Kill Switch", Color.black, Color.red]]).font_(this.prDefaultFont(12, true));
		msgBox.visible = false;

		// store available output devices
		outMenu.items = ServerOptions.outDevices;
		srMenu.items = sampleRates.keys.asArray;

		// button actions
		b1.action = {
			outDevice = outMenu.item.asString;
			sampleRate = sampleRates.at(srMenu.item);
			try{this.prStartServer;}
			{msgText.string_("Error starting the server.");};
			msgText.string_("Device Set to: " + outMenu.item);
			msgBox.visible = true;
		};
		b2.action = {Server.killAll; b2.value = 0; b1.value = 0; msgBox.visible = false;};
	}

	*prDefaultFont {arg size, em;
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