# DB-Legends-Sound-Extraction-Tool
Tool to sort & extract sound files from Dragon Ball Legends (mobile game). Windows only.

You need vgmstream-cli from https://vgmstream.org/ (command-line). Put the contents in a folder named "vgmstream-cli" next to the exe. This is used to extract the files and you don't need the tool if that's all your looking for (Text file in source code includes the commands you need).

## New Python Version

To use the Python version of this tool, execute the following in a terminal (should work on Windows, macOS, or Linux)

```bash
python main.py # for all clips
python main.py CHARACTER LANGUAGE # for one character. CHARACTER is name of character like shallot and LANGUAGE can either be eng or jpn
```

All that is required is Python installed. Also there is a zip file generated at the end named wavs.zip.

Steps: 

\--------------------------------------------------------------------------------------------------------------------

Recommended:

- Don't download all game data and just play with the characters you want the files of then use the tool.

OR (Better)

- A text file with all links for the game's sound files -as of March 10th- and their description is provided by crazydoomy#4471, you can find it above and in the release. Download the files you want, and follow the instructions for the batch file to use on them. I can provide relevant tools to make use of it on request. If you don't want to do that, continue reading.

\----------------------------------------------------------------------------------------------------------------------

The following is if you decide to use the tool on a large number of files:

1 - Transfer the game files from Android >> data >> com.bandainamcoent.dblegends_ww >> files >> streamingassetbundles

2 - Rename all files in the streamingassetbundles folder to have the .acb extension. (A batch file to number them and append the extension will be included)

3 - Run the exe. The tool will sort all possible files based on their contents and create a .bat file to extract them. 

4 - Extract sorted files through the console or by clicking on the generated batch files. You can contact me for a version that automatically does it.

5 - For files that weren't sorted or named correctly, you can check contents manually by opening the .acb file with notepad, but you can still extract them. 

6 - You can then freely delete all .acb files that were extracted. You'll have to delete them manually for the ones that weren't (assets, bgm, bgs & Misc). 

7 - I recommend converting the .wav files to .ogg (with FFmpeg) as they'd be taking up potentially 10x the space otherwise. 

The streamingassetbundles folder includes assets, movies and sound files of types I couldn't directly extract, they'll be put in the right folders.

Author Note: 

There is a lot that could be changed and added to make the sorting nearly perfect (which might be unwanted for some). In the tool's current state you might have to do some manual work when used with a large numbers of files, I however can't promise a better version for at least a few years after my final update to it using C, as this was just to test the language.

Very Specific Notes You Can/Should Ignore: 
- The tool sorts the files based on the description inside the packages, it highly depends on the devs using consistent naming patterns, avoiding mistakes and keeping them separated, and thus it's far from perfect. Extracting the files shows their individual content in their name and should make it easier, and while it's something I can make the tool do, I'd rather not gg. 
- There are very few cases of the sort not working correctly (not using the character name) that I chose to ignore due to their infrequency. As for files that fail to sort, there are currently 90+ of them, this cannot be fixed without a lot of file modification or using more tools, so you'll have to look at them yourself. 
- Backup your files and don't use common names when renaming them in case anything goes wrong. I'm very inexperienced in C and batch files could be dangerous.
- The tool was tested on 19991+ files (everything the japanese version of the game has by March 2023). There shouldn't be any problems as long as the tool has read and write permissions.
- I chose to remove IDs from names, and to avoid collisions, the extraction process will include the original file name at the start, which might make it hard to see the description if the name is long. Contact me if you want a version with the IDs.
- The sorting process could take a few seconds to a minute, the extraction process a minute to several. If the sorting process is taking much longer you can inform me to perhaps consider looking at its efficiency, although I doubt it will be any trouble for anyone. The extraction process is purely done through vgmstream, if you feel like it's using a lot of resources, it's likely an install monitoring app or your antivirus going through the outputs.
- Files that share the exact same name and output folder will be safely ignored, it's right to assume that's the reason some files are not being moved. 
- Audio is mono for most/all files as far as I could tell. You're likely to want to improve their quality before using them in your projects. Some files will have low volume, newer versions of the character will have the correct volume for them.

For any questions you can contact me on discord: ~~LostImbecile#9192~~ lostimbecile.

I'll be happy to help as soon as possible, and will take any suggestions or advice regarding the code or execution process
