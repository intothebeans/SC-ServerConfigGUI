/*TidalStartGUI : ServerConfigGUI {
	*init {
		var fileList, b3, b4, b5, samplePaths;
		var path = Platform.userConfigDir +/+ "sc-sample-paths.save";
		super.init(750);

		fileList = ListView.new(super.win, Rect(300, 100, 425, 200)).selectionMode_(\none).font_(super.prDefaultFont(10, false)).colors_(Color.yellow);

		b3 = Button(super.win, Rect(300,60,140,30)).states_([["Add Path", Color.black, Color.green]]).font_(this.prDefaultFont(12, true));
		b4 = Button(super.win, Rect(450, 60, 140, 30)).string_("Load Sounds").font_(this.prDefaultFont(12, true));

		b5 = Button(super.win, Rect(600, 60, 125, 30)).states_([["Remove Path", Color.black, Color.fromHexString("#ef3939")]]).font_(this.prDefaultFont(12, true));

		if(File.exists(path), {samplePaths = File.readAllString(path).split($;)});

		this.msgBox.visible = false;
		fileList.items = samplePaths;

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

		super.win.front;

	}
}*/

/*	*prStartServer {
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
	}*/

