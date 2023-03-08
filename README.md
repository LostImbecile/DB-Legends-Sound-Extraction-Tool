# DB-Legends-Sound-Extraction-Tool
Tool to extract sound files from Dragon Ball Legends (mobile game). Windows only.

You need vgmstream-cli from https://vgmstream.org/ (command-line). Either put the contents a folder named "vgmstream-cli" next to the exe, or set up its PATH variable.

Steps: 
1 - Rename all files in the streamingassetbundles folder to have the .acb extension.
2 - Run the exe. The tool will sort all possible files based on their contents and create a .bat file to extract them.
3 - Extract sorted files through the console or by clicking on the generated batch files.
4 - For files that weren't sorted or named correctly, you can check contents manually by opening the .acb file with notepad. 
5 - You can then freely delete all .acb files that were extracted. You'll have to delete them manually for the ones that weren't.

The streamingassetbundles folder includes assets, movies and sound files of types I couldn't extract, they'll be put in the right folder as needed.

For any questions you can contact me on discord: LostImbecile#9192
I'll be happy to help as soon as possible
