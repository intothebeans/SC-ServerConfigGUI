TidalStartGUI : ServerConfigGUI {
	classvar samplePaths;
	*start {
		var fileList, b3, b4, b5;
		var path = Platform.userExtensionDir +/+ "sc-sample-paths.save";
		super.prDraw(750);
		fileList = ListView.new(win, Rect(300, 160, 425, 230)).selectionMode_(\extended).font_(super.prDefaultFont(10, false)).colors_(Color.yellow);

		b3 = Button(win, Rect(300, 120,140,30)).states_([["Add Path", Color.black, Color.green]]).font_(super.prDefaultFont);
		b4 = Button(win, Rect(450, 120, 140, 30)).string_("Load Sounds").font_(super.prDefaultFont);
		b5 = Button(win, Rect(600, 120, 125, 30)).states_([["Remove Path", Color.black, Color.fromHexString("#ef3939")]]).font_(super.prDefaultFont);

		if(File.exists(path), {
            samplePaths = File.readAllString(path).split($;); samplePaths.removeAt(samplePaths.size - 1);
        }, {samplePaths = [];});

		fileList.items = samplePaths;

		b3.action = {
			FileDialog({ arg p;
				var f, paths;
				if(samplePaths == nil, {samplePaths = [p ++ "/*"];},
                   { // duplicate check
                    paths = samplePaths.asString;
                        if(paths.contains(p ++ "/*"), {super.prMessageBox("Duplicate Sample Path")},{
                            samplePaths = samplePaths.insert(0, (p ++ "/*"));
                        });
                });
				fileList.items_(samplePaths);
				f = File.open(path, "w");
				samplePaths.do({arg item;
					f.write(item ++ ";");
				});
				f.close();
			}, {"Dialog Cancelled".postln;}, 2, stripResult:true);
		};

        b4.action = {
            if(~dirt != nil, {
                samplePaths.do({|item| ~dirt.loadSoundFiles(item.asString);});},
            {
                super.prMessageBox("Super Dirt Not Running");
            });

        };

        b5.action = {
            var pathSaveFile;
            fileList.selection.collect({|index|
                samplePaths.removeAt(index);
            });
            pathSaveFile = File.open(path, "w");
            samplePaths.do({arg item; pathSaveFile.write(item ++ ";");});
            fileList.items_(samplePaths);

        };

	}
	*prStartServer {
		var s = Server.default;
		if(s.serverRunning, {Server.killAll;});
		super.prStartServer;
		s.waitForBoot {
			~dirt.stop;
			~dirt = SuperDirt(2, s);
			~dirt.loadSoundFiles;
			samplePaths.do({arg item, i;
				~dirt.loadSoundFiles(item.asString);});
			s.sync;
			~dirt.start(57120, 0 ! 12);
			(
				~d1 = ~dirt.orbits[0]; ~d2 = ~dirt.orbits[1]; ~d3 = ~dirt.orbits[2];
				~d4 = ~dirt.orbits[3]; ~d5 = ~dirt.orbits[4]; ~d6 = ~dirt.orbits[5];
				~d7 = ~dirt.orbits[6]; ~d8 = ~dirt.orbits[7]; ~d9 = ~dirt.orbits[8];
				~d10 = ~dirt.orbits[9]; ~d11 = ~dirt.orbits[10]; ~d12 = ~dirt.orbits[11];
			);
		};

		s.latency = 0.3;

	}

}


