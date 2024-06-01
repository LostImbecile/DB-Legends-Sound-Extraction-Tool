# DB-Legends-Sound-Extraction-Tool

**This tool can:**
1. Download all sound files (JPN/ENG/SE/Misc. Your choice).
2. Sort them as perfectly as possible in the form:
     - **Character Name** {Goku}
       - **Character Versions** {SSJ Goku}
          - **GameIDs** {DBL-XX}
            - **Sound Type** {Homescreen}
              - .wav or .ogg
3. Extract them (vgmstream).
4. Convert extracted files (ffmpeg).
5. Delete unnecessary files and folders.
6. Package files into zips, ignoring unwanted formats.

---

**Important Notes:**
- You need `FFmpeg` [Conversion] and `vgmstream-cli` [Extraction]. Make sure they’re either set in PATH (in your environmental variables) or inside a folder in the same directory as the app. Links below.
- To stay up-to-date, run the Python script, which requires Python and UnityPy (`pip install UnityPy`).
- All settings are in `config.properties`. You won’t be asked for input during the run.

**Other Notes:**
- Any task already done won’t be performed again. If a file was downloaded, it detects that and doesn't redownload. Same for extracted or converted files. This saves time and resources. If this causes an issue, delete corrupted files or move them outside the download directory.
- This currently downloads all files in the categories you set (so all ENG/JPN/SE files).
- To extract videos (files without an extension in Misc), use:
  ```
  ffmpeg -i input output.mp4
  ```
  Some files are internally .avi or .usm. No need to add an extension.
- The tool is filename sensitive and will redownload/re-extract any files with changed names.
- Some later processes require full CPU usage, mainly conversion with ffmpeg as your antivirus will be very active.
- Works on Windows and possibly on Linux.
- I recommend turning off anything you don't need in the settings, I made the tool work as thorough as possible but had little time to tweak it, so it can be inefficient.
- For any issues or if it fails contact me on Discord: `lostimbecile`
---

**Links:**
1. [FFmpeg](https://www.gyan.dev/ffmpeg/builds/): Scroll down to release builds and download `ffmpeg-release-full.7z`. Essentials may be enough but untested.
2. [Vgmstream-cli](https://vgmstream.org/): Command-line (64-bit) suffices.
3. [Google Drive](https://drive.google.com/drive/folders/1NB1TviX8Kc1S2LpBpkWzM2tsq7PMBZbN?usp=sharing): Or you can get everything from here including all the sounds (1st of June 2024).

---

**Full process details (You do not need to read this):**

**Downloading:**
- You need an internet connection to download all the required files and connect to dblegends.net, the total size is first checked which may or may not take a while depending on your connection, it doesn't necessarily try to make full use of your bandwidth but it uses a sensible amount through heavy multi-threading, most reasonable I found during my tests.

- If a file was already downloaded it's ignored.

- Downloads will automatically detect if file size is incorrect or if download was cut-off, and re-attempt up to 3 times, after that, the file will simply be deleted and skipped, and you can rerun the tool normally to try again.


**Sorting:**
- Sorting looks at file names, tries to map them to a character from dblegends.net and then sifts them based on my personal observations, while also adding the correct extension (.acb/.awb).

- if a character ends up in the "Other" folder, it's because it's a story/event exclusive character and either has no website equivalent of it (farmer with a shotgun for instance) or the internal ID used for them can't be re-mapped so they all end up there.

- System messages have a Messages folder which isn't sorted into characters, but the file name may include the character's name anyway.

- Non-voice files are put in the right folders (BGM/BGS/VFX/SE).

- If dblegends.net breaks there's a backup that updates on each successful fetch, but the sorting won't work as well.


**Re-sorting:**
- If you move a file (.acb) around a directory and activate re-sort, the file along with all its accompanying files (.ogg/.wav) will be moved to their right location, this feature is simply in case I release a version with an updated sorting algorithm, but there's likely no reason for anyone besides me to use it

**Extraction:**
- There are two kinds of files that are used for extraction, .acb and .awb, I first check if they were already extracted, and then use vgmstream directly, this is multi-threaded and depending on the number of files may take a minute, but not too long.

- If extraction fails, it's because the file has 0 real data in it, simply ignore it.


**Conversion:**
- Converts .wav to .ogg (to save a ton of space)
- Ignores any already converted files by checking if there's an ogg version of them
- This is also multi-threaded and very heavy on the CPU, it may take a while, turning off real-time protection in your antivirus helps if you're comfortable with it.


**Deletion:**
- Deletes any files you set to be deleted, I recommend deleting .wav files if you don't need them, I don't particularly think they have better quality, simply due to the sound container being as small as a regular .ogg file anyway.

**Packaging:**
- Ignores empty folders or folders with only .acb/.awb files.
- Packages files as follows:
  ```
  Pre-package:
  -> Folder
     -> Subfolder1
        -> file1.wav
        -> file2.wav
     -> Subfolder2
        -> file3.acb
     -> Subfolder3

  Into:
  -> Folder.zip
     -> Subfolder1
        -> file1.wav
        -> file2.wav
  ```
- Optionally includes the parent folder:
  ```
  -> Folder.zip/Folder/Subfolder/...
  ```

- This process doesn't usually take long, and it doesn't detect if packaging was done earlier or not.

---
