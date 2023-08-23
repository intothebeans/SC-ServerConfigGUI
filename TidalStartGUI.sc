TidalStartGUI {
	classvar samplePaths, device, sampleRate;

	*start {
		var path = Platform.userConfigDir +/+ "sc-sample-paths.save";
		var win = Window.new("Select Device & Boot", Rect(200, 200, 750, 350),false);
		// create a drop down menu with all the output devices
	    var sampleRates = Dictionary.newFrom(["44.1kHz", 44100, "48kHz", 48000, "96kHz", 96000, "192kHz", 192000]);
		var outMenu = PopUpMenu(win, Rect(10, 10, 400, 30)).resize_(2).font_(this.prDefaultFont(10, false));
		// drop down for changing the sample rate
		var srMenu = PopUpMenu(win, Rect(10, 100, 100, 30)).font_(this.prDefaultFont(10, true));

		// create a box to toggle visibility on button clicks for alerts
		var msgBox = CompositeView.new(win, Rect(10, 140, 400,300));
		var msgText = StaticText.new(msgBox, Rect(0,0, 425, 200)).font_(this.prDefaultFont(14, true)).stringColor_(Color.red);

		// box to list file paths
		var fileList = ListView.new(win, Rect(300, 100, 425, 200)).selectionMode_(\none).font_(this.prDefaultFont(10, false)).colors_(Color.yellow);

		// draw buttons
		var b1 = Button(win, Rect(10,60, 80, 30)).states_([["Boot", Color.black,Color.cyan],["Reboot",Color.blue,Color.white]]).font_(this.prDefaultFont(12, true));

		var b2 = Button(win, Rect(110,60,120,30)).states_([["Kill Switch", Color.black, Color.red]]).font_(this.prDefaultFont(12, true));

		var b3 = Button(win, Rect(300,60,140,30)).states_([["Add Path", Color.black, Color.green]]).font_(this.prDefaultFont(12, true));

		// read data from save file
		if(File.exists(path), {samplePaths = File.readAllString(path).split($;)});
		samplePaths.postln;

		msgBox.visible = false;

		// store available output devices
		fileList.items = samplePaths;
		outMenu.items = ServerOptions.outDevices;
		srMenu.items = sampleRates.keys.asArray;


		// button actions
		b1.action = {
			device = outMenu.item.asString;
			sampleRate = sampleRates.at(srMenu.item);
			try{this.prStartServer;}
			{msgText.string_("Error starting the server.");};
			msgText.string_("Device Set to: " + outMenu.item);
			msgBox.visible = true;
		};
		b3.action = {
			// open file dialog
			FileDialog({ arg p;
				var f;
				if(samplePaths == nil, {samplePaths = [p ++ "/*"];},
					{samplePaths = samplePaths.insert(0, (p ++ "/*"));});
				fileList.items_(samplePaths);
				f = File.open(path, "w");
				samplePaths.do({arg item, i;
					f.write(item ++ ";");
				});
				f.close();
			}, {"Dialog Cancelled".postln;}, 2, stripResult:true);
		};
		b2.action = {Server.killAll; b2.value = 0; b1.value = 0; msgBox.visible = false;};

		win.front;

	}

	*prDefaultFont {arg size, em;
		^Font.new(Font.defaultMonoFace, size, em, usePointSize: true);
	}

	*prStartServer {
		var s = Server.default;
		// code from https://raw.githubusercontent.com/musikinformatik/SuperDirt/develop/superdirt_startup.scd
		if(s.serverRunning, {Server.killAll;});
		s.reboot( {
			// server options are only updated on reboot
			// configure the sound server: here you could add hardware specific options
			// see http://doc.sccode.org/Classes/ServerOptions.html
			s.options.numBuffers = 1024 * 256; // increase this if you need to load more samples
			s.options.memSize = 8192 * 32; // increase this if you get "alloc failed" messages
			s.options.numWireBufs = 64; // increase this if you get "exceeded number of interconnect buffers" messages
			s.options.maxNodes = 1024 * 64; // increase this if you are getting drop outs and the message "too many nodes"
			s.options.numOutputBusChannels = 2; // set this to your hardware output channel size, if necessary
			s.options.numInputBusChannels = 2; // set this to your hardware output channel size, if necessary
			s.options.outDevice = device;
			s.options.sampleRate = sampleRate;
			// boot the server and start SuperDirt
			s.waitForBoot {
				~dirt.stop; // stop any old ones, avoid duplicate dirt (if it is nil, this won't do anything)
				~dirt = SuperDirt(2, s); // two output channels, increase if you want to pan across more channels
				~dirt.loadSoundFiles;   // load samples (path containing a wildcard can be passed in)
				samplePaths.do({arg item, i; item.asString.postln; ~dirt.loadSoundFiles(item.asString);});
				// for example: ~dirt.loadSoundFiles("/Users/myUserName/Dirt/s amples/*");
				s.sync; // optionally: wait for samples to be read
				~dirt.start(57120, 0 ! 12);   // start listening on port 57120, create two busses each sending audio to channel 0

				// optional, needed for convenient access from sclang:
				(
					~d1 = ~dirt.orbits[0]; ~d2 = ~dirt.orbits[1]; ~d3 = ~dirt.orbits[2];
					~d4 = ~dirt.orbits[3]; ~d5 = ~dirt.orbits[4]; ~d6 = ~dirt.orbits[5];
					~d7 = ~dirt.orbits[6]; ~d8 = ~dirt.orbits[7]; ~d9 = ~dirt.orbits[8];
					~d10 = ~dirt.orbits[9]; ~d11 = ~dirt.orbits[10]; ~d12 = ~dirt.orbits[11];
				);
			};

			s.latency = 0.3; // increase this if you get "late" messages


		}, {Exception("There was an issue starting the server");});
	}

}
