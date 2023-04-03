(
	x = {
		var win,drop,b1,b2,b3,t,text,o,val;

		// create basic window that can't be resized
		win = Window.new("Select Device & Boot", Rect(200, 200, 450, 350),false);

		// store available output devices
		o = ServerOptions.outDevices;

		// create a drop down menu with all the output devices
		drop = PopUpMenu(win, Rect(10, 10, 400, 30)).resize_(2).font_(Font.new(Font.defaultMonoFace, 10, false, usePointSize: true));
		drop.items = o;

		// create a box to toggle visibility on button clicks for alerts
		t = CompositeView.new(win, Rect(10, 120, 430,300));
		t.visible = false;
		text = StaticText.new(t, Rect(0,0, 425, 200)).font_(Font.new(Font.defaultMonoFace, 14, true, usePointSize: true)).stringColor_(Color.red);

		// draw buttons
		b1 = Button(win, Rect(10, 60, 60, 30)).states_([["Set",Color.black,Color.green], ["Reset"]]).font_(Font.new(Font.defaultMonoFace, 12, true, usePointSize: true));
		b2 = Button(win, Rect(100,60, 80, 30)).states_([["Boot", Color.black,Color.cyan], ["Reboot",Color.blue,Color.white]]).font_(Font.new(Font.defaultMonoFace, 12, true, usePointSize: true));
		b3 = Button(win, Rect(210,60,120,30)).states_([["Kill Switch", Color.black, Color.red]]).font_(Font.new(Font.defaultMonoFace, 12, true, usePointSize: true));

		// button actions
		b1.action = {
			if(b1.value != 1,
				{b1.value = 0; text.string_("Remember to reboot the server to apply new settings");},
				{val = drop.value;
				b1.value = 1;
				if (val != nil,{text.string_("Device Set to:/n" + drop.item); t.visible = true});});


		};

		// code from https://raw.githubusercontent.com/musikinformatik/SuperDirt/develop/superdirt_startup.scd
		b2.action = {
			s.killAll;
			s.reboot( {
				// server options are only updated on reboot
				// configure the sound server: here you could add hardware specific options
				// see http://doc.sccode.org/Classes/ServerOptions.html
				s.options.numBuffers = 1024 * 256; // increase this if you need to load more samples
				s.options.memSize = 8192 * 32; // increase this if you get "alloc failed" messages
				s.options.numWireBufs = 64; // increase this if you get "exceeded number of interconnect buffers" messages
				s.options.maxNodes = 1024 * 32; // increase this if you are getting drop outs and the message "too many nodes"
				s.options.numOutputBusChannels = 2; // set this to your hardware output channel size, if necessary
				s.options.numInputBusChannels = 2; // set this to your hardware output channel size, if necessary
				s.options.outDevice = drop.item;
				// boot the server and start SuperDirt
				s.waitForBoot {
					~dirt.stop; // stop any old ones, avoid duplicate dirt (if it is nil, this won't do anything)
					~dirt = SuperDirt(2, s); // two output channels, increase if you want to pan across more channels
					~dirt.loadSoundFiles;   // load samples (path containing a wildcard can be passed in)
					// for example: ~dirt.loadSoundFiles("/Users/myUserName/Dirt/samples/*");
					// s.sync; // optionally: wait for samples to be read
					~dirt.start(57120, 0 ! 12);   // start listening on port 57120, create two busses each sending audio to channel 0

					// optional, needed for convenient access from sclang:
					(
						~d1 = ~dirt.orbits[0]; ~d2 = ~dirt.orbits[1]; ~d3 = ~dirt.orbits[2];
						~d4 = ~dirt.orbits[3]; ~d5 = ~dirt.orbits[4]; ~d6 = ~dirt.orbits[5];
						~d7 = ~dirt.orbits[6]; ~d8 = ~dirt.orbits[7]; ~d9 = ~dirt.orbits[8];
						~d10 = ~dirt.orbits[9]; ~d11 = ~dirt.orbits[10]; ~d12 = ~dirt.orbits[11];
					);

					// directly below here, in your own copy of this file, you could add further code that you want to call on startup
					// this makes sure the server and ~dirt are running
					// you can keep this separate and make it easier to switch between setups
					// by using "path/to/my/file.scd".load and if necessary commenting out different load statements
					// ...

				};

				s.latency = 0.3; // increase this if you get "late" messages


		}, {text.string_("There was an issue starting the server");});
		};
		b3.action = {Server.killAll; b2.value = 0; b1.value = 0; t.visible = false;};


		win.front;
	};
	x.value();
);


