import os
import requests
import platform
import zipfile
import tarfile
import sys

num_arguments = len(sys.argv) - 1  # subtract 1 to exclude the script name itself

number_found = 0

all_characters = False
if num_arguments == 0:
    choice = input('WARNING: You have specified no character or language.\n'
                   'The way to use this program is:\npython main.py CHARACTER_NAME LANGUAGE\n'
                   'Your input means that the tool will download EVERY SOUND FILE.\n'
                   'Is this what you want? Type y for yes, and press Enter.\n'
                   'Or type something else if not.\n')
    if choice in ['y', 'Y', 'yes', 'YES', 'ya']:
        print('Continuing')
        all_characters = True
    else:
        print('Quitting')
        exit()

elif num_arguments > 0:
    if num_arguments != 2:
        print('Wrong format. The way to use this program is:\npython main.py CHARACTER_NAME LANGUAGE'
              '\nAlso, language can be either eng or jpn')
        exit()
    if sys.argv[1] == 'CHARACTER_NAME':
        print('no you silly goose, put the name of a character like vegeta')
        exit()
    if sys.argv[2] == 'LANGUAGE' or sys.argv[2] not in ['eng', 'jpn']:
        print('no you silly goose, put eng or jpn')
        exit()
    character = sys.argv[1]
    language = sys.argv[2]

def make_folder(dir_name):
    if not os.path.exists(dir_name):
        try:
            os.makedirs(dir_name)
            print(f"Folder '{dir_name}' created successfully.")
        except OSError as e:
            print(f"Error occurred while creating folder: {e}")
    else:
        print(f"Folder already exists.")


def vgmstream_dl():
    system = platform.system()

    if system == "Windows":
        link = "https://github.com/vgmstream/vgmstream-releases/releases/download/nightly/vgmstream-win64.zip"
        ext = 'zip'
    elif system == "Darwin":
        link = "https://github.com/vgmstream/vgmstream-releases/releases/download/nightly/vgmstream-mac-cli.tar.gz"
        ext = 'tar.gz'
    elif system == "Linux":
        link = "https://github.com/vgmstream/vgmstream-releases/releases/download/nightly/vgmstream-linux-cli.tar.gz"
        ext = 'tar.gz'
    else:
        raise NotImplementedError(f'Unsupported computer os, what os do you have? Ask for {system} support')
    try:
        response = requests.get(link)
        response.raise_for_status()  # raise an exception if the request was not successful
        with open(f'vgmstream.{ext}',
                  'wb') as file:
            file.write(response.content)
        # print("Download completed successfully.")
    except requests.exceptions.RequestException as e:
        print(f"Error occurred: {e}")

    make_folder('vgmstream')
    file_path = f'vgmstream.{ext}'
    if ext == "zip":
        with zipfile.ZipFile(file_path, "r") as zip_ref:
            zip_ref.extractall('vgmstream/')
        print(f"vgmstream was successfully unzipped.")
    elif ext == "tar.gz":
        with tarfile.open(file_path, "r:gz") as tar_ref:
            tar_ref.extractall('vgmstream/')
        print(f"vgmstream was successfully untarred.")
    else:
        print(f"Unsupported file ext: {ext}")

    if os.path.exists(file_path):
        os.remove(file_path)
        print(f"{file_path} has been deleted.")
    else:
        print(f"{file_path} not found. No action taken.")

def convert_acb_to_wav():
    acb_folder = "acb_files"
    wav_folder = "wav_files"

    # get the correct vgmstream-cli executable based on the operating system
    system = platform.system()
    vgmstream_cli = r"vgmstream\vgmstream-cli.exe" if system == 'Windows' else "./vgmstream/vgmstream-cli.exe"

    for root, _, files in os.walk(acb_folder):
        for filename in files:
            if filename.endswith(".acb"):
                full_path = os.path.join(root, filename)
                y = os.path.splitext(filename)[0]  # Remove the extension from the filename
                output_file = os.path.join(wav_folder, f"{y}_01_03s_?n.wav")
                command = f"{vgmstream_cli} -S 0 -o \"{output_file}\" \"{full_path}\""
                os.system(command)

#
# MAIN CODE
#
# get vgmstream
if not os.path.exists('vgmstream/'):
    print("Oh no, you don't have vgmstream.\nInstalling now...\n\n")
    vgmstream_dl()


# make folder
folders_to_make = ['wav_files', 'acb_files']
for folder in folders_to_make:
    if not os.path.exists(folder):
        try:
            os.makedirs(folder)
            print(f"Folder '{folder}' created successfully.")
        except OSError as e:
            print(f"Error occurred while creating folder: {e}")
    else:
        print(f"Folder already exists.")

# read txt file
try:
    with open('Links_To_Specific_Files.txt', 'r') as file:
        thebigfile = file.read()
except FileNotFoundError:
    print(f"File not found.")
    exit()
except IOError as e:
    print(f"Error occurred while reading the file: {e}")

thebigfile = thebigfile.splitlines()

print(type(thebigfile))
number_of_lines = len(thebigfile)
for index, line in enumerate(thebigfile):
    result_tuple = False
    if all_characters is False:
        if character in line and language in line:
            number_found += 1
            # print(line)
            # find the first colon occurrence
            colon_index = line.find(':')

            # extract key and value parts
            key = line[:colon_index].strip().replace('"', '')
            value = line[colon_index + 1:].strip().replace('"', '')

            # create a tuple
            # it's [:-1] to take out last comma
            result_tuple = (key, value[:-1])
    else:
        if 'streamingassetbundles' in line:
            number_found += 1
            # find the first colon occurrence
            colon_index = line.find(':')

            # extract key and value parts
            key = line[:colon_index].strip().replace('"', '')
            value = line[colon_index + 1:].strip().replace('"', '')

            # create a tuple
            # it's [:-1] to take out last comma
            result_tuple = (key, value[:-1])

    # print(result_tuple)
    print(f'{index + 1}/{number_of_lines}')
    if result_tuple is not False:
        try:
            response = requests.get(result_tuple[1])
            response.raise_for_status()  # raise an exception if the request was not successful

            with open(os.path.join('acb_files/', (os.path.basename(result_tuple[0]) + '.acb')), 'wb') as file:
                file.write(response.content)
            # print("Download completed successfully.")
        except requests.exceptions.RequestException as e:
            print(f"Error occurred: {e}")

convert_acb_to_wav()

source_folder = "wav_files"
output_zip = "wavs.zip"

with zipfile.ZipFile(output_zip, 'w') as zipf:
    for foldername, _, filenames in os.walk(source_folder):
        for filename in filenames:
            file_path = os.path.join(foldername, filename)
            arcname = os.path.relpath(file_path, source_folder)
            zipf.write(file_path, arcname)

if number_found != 0:
    print('\nim goku... im insane... from earth.\n(done)')
else:
    print(f'\nOh no... No character files were found for {character}.\nDid you misspell?')
