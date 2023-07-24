# DB-Legends-Sound-Extraction-Tool
Tool to sort & extract sound files from Dragon Ball Legends (mobile game). C version is windows only.

For the exe version you need vgmstream-cli from https://vgmstream.org/ (command-line). Put the contents in a folder named "vgmstream-cli" next to the exe. This is used to extract the files and you don't need the tool if that's all your looking for (Text file in source code includes the commands you need).

## New Python Version 
To use the Python version of this tool, execute the following in a terminal (should work on Windows, macOS, or Linux)

```bash
python main.py # for all clips
python main.py CHARACTER LANGUAGE # for one character. CHARACTER is name of character like shallot and LANGUAGE can either be eng or jpn
```

All that is required is Python installed. Also there is a zip file generated at the end named wavs.zip. 

Many thanks to jpfleischer!


## Old Exe Version

The Python version is highly recommended as it contains all you need if you include the text file with the links.

(You can skip all these steps if you use it)

\----------------------------------------------------------------------------------------------------------------------

Steps: 

- Don't download all game data and just play with the characters you want the files of then use the tool.

OR

- A text file with all links for the game's sound files -as of March 10th- and their description is provided by crazydoomy, you can find it above and in the release. Download the files you want, and follow the instructions for the batch file to use on them. I can provide relevant tools to make use of it on request. If you don't want to do that, continue reading.

\----------------------------------------------------------------------------------------------------------------------

1 - Transfer the game files from Android >> data >> com.bandainamcoent.dblegends_ww >> files >> streamingassetbundles

2 - Rename all files in the streamingassetbundles folder to have the .acb extension. (A batch file to number them and append the extension will be included)

3 - Run the exe. The tool will sort all possible files based on their contents and create a .bat file to extract them. 

4 - Extract sorted files through the console or by clicking on the generated batch files. You can contact me for a version that automatically does it.

5 - For files that weren't sorted or named correctly, you can check contents manually by opening the .acb file with notepad, but you can still extract them. 

6 - You can then freely delete all .acb files that were extracted. You'll have to delete them manually for the ones that weren't (assets, bgm, bgs & Misc). 

7 - I recommend converting the .wav files to .ogg (with FFmpeg) as they'd be taking up potentially 10x the space otherwise. 

The streamingassetbundles folder includes assets, movies and sound files of types I couldn't directly extract, they'll be put in the right folders.

Notes: 
- Files that share the exact same name and output folder will be safely ignored, it's right to assume that's the reason some files are not being moved. 
- Audio is mono for most/all files as far as I could tell. You're likely to want to improve their quality before using them in your projects. Some files will have low volume, newer versions of the character will have the correct volume for them.

For any questions you can contact me on discord: ~~LostImbecile#9192~~ lostimbecile.

I'll be happy to help as soon as possible
