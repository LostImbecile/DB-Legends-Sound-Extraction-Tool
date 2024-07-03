#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <ctype.h>
#include <time.h>

// If you can give me advice on how to make the code less rubbish I'd be forever grateful
void createDirectory(char[], FILE* log);
void moveOutputFile(char[], char[], char[], FILE* vgmstream, FILE* delbatch, FILE* log);
void createOutputFolder(char[], char[], char[], const char*, FILE* vgmstream, FILE* delbatch, FILE* log);
char* replace_char(char[]);

int main()
{
    FILE *log  = fopen("log.txt", "w");
    if (log == NULL)
    {
        printf("Error : Failed to open log file - %s\n", strerror(errno));
        return 1;
    }
    clock_t count_time = clock();
    fprintf(log,"Process Started\n");
    createDirectory("vgmstream-cli",log);
    DIR *d;
    struct dirent *dir;
    FILE *entry_file;
    char entry_path[100];
    char input_dir[10] = "./Input";
    createDirectory(input_dir,log);

    int bufferLength = 200;
    char buffer[bufferLength];
    const char *path;

    char output_path[100];
    char output_dir[15] = "./Extracted/";
    char output_folder[40];
    int success_counter = 0;
    createDirectory(output_dir,log);

    int failed_to_sort = 0;
    int failed_counter = 0;
    char ftemp[6] = "";
    char ftemp1[2] = "";
    char ftemp2[2] = "";
    char ftemp3[2] = "";
    int not_sound_counter = 0;

    FILE *extract_sorted_bat = fopen("Extract_Sorted.bat","w");
    if (extract_sorted_bat == NULL)
    {
        fprintf(log, "Error : Failed to open or create extraction .bat file - %s\n", strerror(errno));
        fclose(log);
        return 1;
    }
    FILE *del_sorted_bat = fopen("Delete_Extracted_ACB_Files.bat","w");
    if (del_sorted_bat == NULL)
    {
        fprintf(log, "Error : Failed to open or create delete .bat file - %s\n", strerror(errno));
        fclose(log);
        return 1;
    }

    FILE *extract_failed_bat = fopen("Extract_Failed.bat","w");
    if (extract_failed_bat == NULL)
    {
        fprintf(log, "Error : Failed to open or create extraction .bat file - %s\n", strerror(errno));
        fclose(log);
        return 1;
    }

    FILE *del_failed_bat = fopen("Delete_Failed_ACB_Files.bat","w");
    if (del_failed_bat == NULL)
    {
        fprintf(log, "Error : Failed to open or create delete .bat file - %s\n", strerror(errno));
        fclose(log);
        return 1;
    }

    d = opendir(input_dir);
    if (d)
    {
        while ((dir = readdir(d)) != NULL)
        {
            path = dir->d_name;

            if (strstr(path,".acb")!=NULL)
            {
                strcpy(entry_path,input_dir);
                strcat(entry_path,"/");
                strcat(entry_path,path);
                entry_file = fopen(entry_path, "r");

                if (entry_file != NULL)
                {
                    printf("\n%s\n",path);
                    fprintf(log,"\n%s\n",path);
                    fgets(buffer, 5, entry_file);

                    if(strstr(buffer,"@UTF") == NULL)
                    {
                        fclose(entry_file);

                        puts("Not a sound file\n");
                        fprintf(log,"Not a sound file\n");
                        fflush(log);

                        strcpy(output_path,output_dir);

                        strcat(output_path,"Assets-Movies/");

                        strcpy(output_folder,output_path);

                        strcat(output_path,path);

                        createDirectory(output_folder,log);
                        moveOutputFile(entry_path,output_path,output_folder,NULL,NULL,log);
                        not_sound_counter++;
                        fflush(log);
                        continue;
                    }
                    else
                    {
                        failed_to_sort = 1;
                        while(fgets(buffer, bufferLength, entry_file))
                        {
                            if(strstr(buffer,"Build:") != NULL)
                            {
                                strcpy(buffer,"");
                                // Miss-encoded EOL
                                fgets(buffer, 2, entry_file);
                                fgets(buffer, 80, entry_file);

                                fclose(entry_file);
                                puts(buffer);
                                fprintf(log,"%s\n",buffer);
                                createOutputFolder(buffer,output_dir,entry_path,path,extract_sorted_bat,del_sorted_bat,log);
                                failed_to_sort = 0;
                                success_counter++;
                                fprintf(log,"Sorted\n");
                                fflush(log);
                                break;
                            }
                        }
                        fclose(entry_file);
                        if(failed_to_sort)
                        {
                            failed_counter++;

                            strcpy(output_path,"./Failed/");
                            createDirectory("Failed",log);

                            if(failed_counter<10)
                            {
                                ftemp1[0] = failed_counter + '0';
                                strcpy(ftemp,ftemp1);
                            }
                            else if(failed_counter<100)
                            {
                                ftemp1[0] = failed_counter%10 + '0';
                                ftemp2[0] = failed_counter/10 + '0';
                                strcpy(ftemp,ftemp2);
                                strcat(ftemp,ftemp1);
                            }
                            else
                            {
                                ftemp1[0] = failed_counter%10 + '0';
                                ftemp2[0] = (failed_counter%100)/10 + '0';
                                ftemp3[0] = failed_counter/100 + '0';
                                strcpy(ftemp,ftemp3);
                                strcat(ftemp,ftemp2);
                                strcat(ftemp,ftemp1);
                            }

                            strcat(output_path,ftemp);
                            strcat(output_path,"/");

                            strcpy(output_folder,output_path);

                            strcat(output_path,path);

                            createDirectory(output_folder,log);
                            moveOutputFile(entry_path,output_path,output_folder,extract_failed_bat,del_failed_bat,log);
                            fprintf(log,"Failed to sort\n");
                            fflush(log);
                        }
                    }
                }
                else
                {
                    fprintf(log, "Error : Failed to open entry file - %s\n", strerror(errno));
                }
            }
        }

        fprintf(extract_failed_bat,"pause");
        fclose(extract_failed_bat);

        fprintf(del_failed_bat,"pause");
        fclose(del_failed_bat);

        fprintf(del_sorted_bat,"pause");
        fclose(del_sorted_bat);

        fprintf(extract_sorted_bat,"pause");
        fclose(extract_sorted_bat);

        closedir(d);

    }

    count_time = clock() - count_time;
    double time_taken_for_sort = ((double)count_time)/CLOCKS_PER_SEC;
    if(success_counter>0 || not_sound_counter>0 || failed_counter>0)
    {
        printf("\n>> Process time: %.2lf seconds\n",time_taken_for_sort);
        fprintf(log,"\n>> Process time: %.2lf seconds\n",time_taken_for_sort);
        if(not_sound_counter>0)
        {
            printf("\n%d Files aren't sound files. You can find them in the Assets-Movies folder\n",not_sound_counter);
            fprintf(log,"\n%d Files aren't sound files. You can find them in the Assets-Movies folder\n",not_sound_counter);
        }
        if(failed_counter>0)
        {
            printf("\n%d Files weren't sorted. You can try to extract them through Extract_Failed.bat.\n(You should manually sort them before deleting the .acb files)\n",failed_counter);
            fprintf(log,"\n%d Files weren't sorted. You can try to extract them through Extract_Failed.bat\n(You should manually sort them before deleting the .acb files)\n",failed_counter);
        }
        if(success_counter>0)
        {
            printf("\n%d Files were successfully sorted.\n",success_counter);
            fprintf(log,"\n%d Files were successfully sorted.\n",success_counter);
            int x;
            printf("\n>> Extract sorted files now? 1 = yes , 0 = no\n");
            scanf("%d",&x);
            if(x==1)
            {
                clock_t count_time_extract = clock();

                system("Extract_Sorted.bat");

                count_time_extract = clock() - count_time_extract;
                double time_taken_for_extract = ((double)count_time_extract)/CLOCKS_PER_SEC;

                printf("\n>> Extracting files took %.2lf seconds.\n",time_taken_for_extract);
                fprintf(log,"\n>> Sorted Files were successfully extracted taking a total of %.2lf seconds.\n",time_taken_for_extract);
                getchar();
            }
            else
            {
                printf("\nYou can try to extract them through Extract_Sorted.bat when you're ready.\n");
                fprintf(log,"\nSorted Files were not extracted. You can try to extract them through Extract_Sorted.bat when you're ready.\n");

            }
        }

        fprintf(log,"\nNote: If extraction fails make sure you have vgmstream-cli.exe in the right folder");
    }
    else
    {
        printf("Nothing was processed\n");
        fprintf(log,"Nothing was processed\n");
    }


    fclose(log);

    printf("\nPress Enter to close...");
    getchar();

    return(0);
}
void createDirectory(char name[], FILE* log)
{
    int out_dir = mkdir(name);
    if(out_dir && errno != EEXIST)
    {
        printf("Error : Failed to create directory %s -  %s\n", name, strerror(errno));
        fprintf(log,"Error : Failed to create directory %s -  %s\n", name, strerror(errno));
        fflush(log);
    }
}
void moveOutputFile(char name[], char newpath[], char folder[], FILE *vgmstream, FILE* del, FILE* log)
{
    printf("Moving File to %s\n",newpath);
    fprintf(log,"Moving File to %s\n",newpath);
    int a = rename(name,newpath);
    if (a)
    {
        printf("Error : Failed to move output file - %s\n", strerror(errno));
        fprintf(log,"Error : Failed to move output file - %s\n", strerror(errno));
        return;
    }
    if(vgmstream!=NULL)
    {
        strcpy(newpath,replace_char(newpath));
        fprintf(vgmstream,"\".\\vgmstream-cli\\vgmstream-cli\" -S 0 -o \"?f?03s_?n.wav\" \"%s\"\n",newpath);
        fflush(vgmstream);

        if(del!=NULL)
        {
            fprintf(del,"del \"%s\"\n",newpath);
            fflush(del);
        }
    }
}
void createOutputFolder(char name[],char output_directory[], char old_path[], const char* old_name,FILE* vgmstream, FILE* del, FILE* log)
{
    char out_name[200];
    strcpy(out_name,output_directory);

    char folder[100];
    int might_not_extract_flag = 0;

    if(strstr(name,"_se")!=NULL || strstr(name,"cmn")!=NULL || strstr(name,"detailed")!=NULL)
    {
        strcat(out_name,"Sound Effects/");
    }
    else
    {
        char *token = strtok(name, "_");

        char temp[20];
        char temp_token[15];

        if(strcmp(token,"cha")==0 || strcmp(token,"win")==0 || strcmp(token,"RisingRush")==0 || strcmp(token,"cutin")==0 || strstr(token,"Provo")!=NULL || strcmp(token,"formchange") == 0 )
        {
            strcpy(temp_token,token);
            token = strtok(NULL, "_");
            token = strtok(NULL, "_");
            token[0] = toupper(token[0]);
            strcat(out_name,token);

            createDirectory(out_name,log);

            if(strcmp(temp_token,"cha")==0)
                strcpy(temp,"/Battle/");
            else if(strcmp(temp_token,"win")==0)
                strcpy(temp,"/Winscreen/");
            else if(strcmp(temp_token,"RisingRush")==0)
                strcpy(temp,"/Rising Rush/");
            else if(strcmp(temp_token,"cutin")==0)
                strcpy(temp,"/Main Ability/");
            else if(strstr(temp_token,"Provo")!=NULL)
                strcpy(temp,"/Provocation/");
            else
                strcpy(temp,"/Transformation/");

            strcat(out_name,temp);
        }
        else if(strcmp(token,"bgm")==0)
        {
            strcat(out_name,"Background Music/");
            might_not_extract_flag = 1;
        }
        else if(strcmp(token,"bgs")==0)
        {
            strcat(out_name,"Background Sounds/");
            might_not_extract_flag = 1;
        }
        else if(strcmp(token,"str")==0)
        {
            strcat(out_name,"Story/");
        }
        else if(strcmp(token,"gacha")==0)
        {
            token = strtok(NULL, "_");
            if(strcmp(token,"vc")==0)
            {
                strcat(out_name,"/Summon/");
                createDirectory(out_name,log);

                token = strtok(NULL, "_");
                token[0] = toupper(token[0]);

                strcat(out_name,token);
                strcat(out_name,"/");
            }
            else
            {
                if(strcmp(token,"card")==0)
                {
                    for(int i=0; i<2; i++)
                        token = strtok(NULL, "_");

                    token[0] = toupper(token[0]);
                    strcat(out_name,token);

                }
                else
                {
                    token = strtok(NULL, "_");
                    token[0] = toupper(token[0]);
                    strcat(out_name,token);
                }

                createDirectory(out_name,log);

                strcat(out_name,"/Homescreen/");
            }

        }
        else if(strcmp(token,"change")==0)
        {
            for(int i=0; i<2; i++)
                token = strtok(NULL, "_");

            if(strcmp(token,"special")==0)
                strcpy(temp,"/Special Change/");
            else if(strcmp(token,"norm")==0)
                strcpy(temp,"/Normal Change/");
            else
                strcpy(temp,"/Cover Rescue/");

            token = strtok(NULL, "_");
            token[0] = toupper(token[0]);
            strcat(out_name,token);

            createDirectory(out_name,log);

            strcat(out_name,temp);
        }
        else if(strcmp(token,"appear")==0)
        {
            for(int i=0; i<2; i++)
                token = strtok(NULL, "_");

            if(strcmp(token,"rival")==0)
                strcpy(temp,"/Special Entrance/");
            else
                strcpy(temp,"/Normal Entrance/");

            token = strtok(NULL, "_");
            token[0] = toupper(token[0]);
            strcat(out_name,token);

            createDirectory(out_name,log);

            strcat(out_name,temp);
        }
        else if(strcmp(token,"arts")==0)
        {
            for(int i=0; i<2; i++)
                token = strtok(NULL, "_");

            token[0] = toupper(token[0]);
            strcat(out_name,token);

            token = strtok(NULL, "_");
            if(strcmp(token,"SP")==0)
                strcpy(temp,"/Special Move/");
            else if(strcmp(token,"EX")==0)
                strcpy(temp,"/Greencard/");
            else if(strcmp(token,"UT")==0)
                strcpy(temp,"/Ultimate/");
            else
                strcpy(temp,"/Special Card/");

            for(int i=0; i<2; i++)
                token = strtok(NULL, "_");

            createDirectory(out_name,log);

            strcat(out_name,temp);
        }
        else if(strcmp(token,"intro")== 0 || strcmp(token,"spine")== 0)
        {
            for(int i=0; i<3; i++)
                token = strtok(NULL, "_");

            token[0] = toupper(token[0]);

            strcat(out_name,token);

            createDirectory(out_name,log);

            strcat(out_name,"/Intro/");
        }
        else  if(strcmp(token,"sys")==0)
        {
            strcat(out_name,"Messages/");
        }
        else
        {
            strcat(out_name,"Miscellaneous/");
            might_not_extract_flag = 1;
        }
    }

    strcpy(folder,out_name);
    createDirectory(out_name,log);
    strcat(out_name,old_name);

    if(might_not_extract_flag)
        moveOutputFile(old_path,out_name,folder,vgmstream, NULL,log);
    else
        moveOutputFile(old_path,out_name,folder,vgmstream, del,log);

}
char* replace_char(char str[])
{
    char *current_pos = strchr(str,'/');
    while (current_pos)
    {
        *current_pos = '\\';
        current_pos = strchr(current_pos,'/');
    }
    return str;
}
