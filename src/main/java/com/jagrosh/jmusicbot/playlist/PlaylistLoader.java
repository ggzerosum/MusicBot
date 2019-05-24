/*
 * Copyright 2018 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.playlist;

import com.jagrosh.jmusicbot.BotConfig;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlaylistLoader
{
    private final BotConfig config;
    
    public PlaylistLoader(BotConfig config)
    {
        this.config = config;
    }
    
    public List<String> getPlaylistNames()
    {
        if(folderExists())
        {
            File folder = new File(config.getPlaylistsFolder());
            return Arrays.asList(folder.listFiles((pathname) -> pathname.getName().endsWith(".txt")))
                    .stream().map(f -> f.getName().substring(0,f.getName().length()-4)).collect(Collectors.toList());
        }
        else
        {
            createFolder();
            return Collections.EMPTY_LIST;
        }
    }
    
    public void createFolder()
    {
        try
        {
            Files.createDirectory(Paths.get(config.getPlaylistsFolder()));
        } 
        catch (IOException ignore) {}
    }
    
    public boolean folderExists()
    {
        return Files.exists(Paths.get(config.getPlaylistsFolder()));
    }
    
    public void createPlaylist(String name) throws IOException
    {
        Files.createFile(Paths.get(config.getPlaylistsFolder()+File.separator+name+".txt"));
    }
    
    public void deletePlaylist(String name) throws IOException
    {
        Files.delete(Paths.get(config.getPlaylistsFolder()+File.separator+name+".txt"));
    }
    
    public void writePlaylist(String name, String text) throws IOException
    {
        Files.write(Paths.get(config.getPlaylistsFolder()+File.separator+name+".txt"), text.trim().getBytes());
    }
    
    public Playlist getPlaylist(String name)
    {
        if(!getPlaylistNames().contains(name))
            return null;
        try
        {
            if(folderExists())
            {
                boolean[] shuffle = {false};
                List<String> list = new ArrayList<>();
                Files.readAllLines(Paths.get(config.getPlaylistsFolder()+File.separator+name+".txt")).forEach(str -> 
                {
                    String s = str.trim();
                    if(s.isEmpty())
                        return;
                    if(s.startsWith("#") || s.startsWith("//"))
                    {
                        s = s.replaceAll("\\s+", "");
                        if(s.equalsIgnoreCase("#shuffle") || s.equalsIgnoreCase("//shuffle"))
                            shuffle[0]=true;
                    }
                    else
                        list.add(s);
                });
                if(shuffle[0])
                    shuffle(list);
                return new Playlist(name, list, shuffle[0]);
            }
            else
            {
                createFolder();
                return null;
            }
        }
        catch(IOException e)
        {
            return null;
        }
    }
    
    
    private static <T> void shuffle(List<T> list)
    {
        for(int first =0; first<list.size(); first++)
        {
            int second = (int)(Math.random()*list.size());
            T tmp = list.get(first);
            list.set(first, list.get(second));
            list.set(second, tmp);
        }
    }
    
    
    public class Playlist
    {
        private final String name;
        private final List<String> items;
        private final boolean shuffle;
        private final List<AudioTrack> audioTracks = new LinkedList<>();
        private final List<PlaylistLoadError> playlistLoadErrors = new LinkedList<>();
        private boolean loaded = false;
        
        private Playlist(String name, List<String> items, boolean shuffle)
        {
            this.name = name;
            this.items = items;
            this.shuffle = shuffle;
        }
        
        public void loadTracks(AudioPlayerManager audioPlayerManager, Consumer<AudioTrack> consumer, Runnable callback)
        {
            if(!loaded)
            {
                loaded = true;
                for(int i=0; i<items.size(); i++)
                {
                    boolean last = i+1==items.size();
                    int index = i;
                    audioPlayerManager.loadItemOrdered(name, items.get(i), new AudioLoadResultHandler()
                    {
                        @Override
                        public void trackLoaded(AudioTrack audioTrack)
                        {
                            if(config.isTooLong(audioTrack))
                                playlistLoadErrors.add(new PlaylistLoadError(index, items.get(index), "This track is longer than the allowed maximum"));
                            else
                            {
                                audioTrack.setUserData(0L);
                                audioTracks.add(audioTrack);
                                consumer.accept(audioTrack);
                            }
                            if(last && callback!=null)
                                callback.run();
                        }
                        
                        @Override
                        public void playlistLoaded(AudioPlaylist audioPlaylist)
                        {
                            if(audioPlaylist.isSearchResult())
                            {
                                if(config.isTooLong(audioPlaylist.getTracks().get(0)))
                                    playlistLoadErrors.add(new PlaylistLoadError(index, items.get(index), "This track is longer than the allowed maximum"));
                                else
                                {
                                    audioPlaylist.getTracks().get(0).setUserData(0L);
                                    audioTracks.add(audioPlaylist.getTracks().get(0));
                                    consumer.accept(audioPlaylist.getTracks().get(0));
                                }
                            }
                            else if(audioPlaylist.getSelectedTrack()!=null)
                            {
                                if(config.isTooLong(audioPlaylist.getSelectedTrack()))
                                    playlistLoadErrors.add(new PlaylistLoadError(index, items.get(index), "This track is longer than the allowed maximum"));
                                else
                                {
                                    audioPlaylist.getSelectedTrack().setUserData(0L);
                                    audioTracks.add(audioPlaylist.getSelectedTrack());
                                    consumer.accept(audioPlaylist.getSelectedTrack());
                                }
                            }
                            else
                            {
                                List<AudioTrack> loaded = new ArrayList<>(audioPlaylist.getTracks());
                                if(shuffle)
                                    for(int first =0; first<loaded.size(); first++)
                                    {
                                        int second = (int)(Math.random()*loaded.size());
                                        AudioTrack tmp = loaded.get(first);
                                        loaded.set(first, loaded.get(second));
                                        loaded.set(second, tmp);
                                    }
                                loaded.removeIf(track -> config.isTooLong(track));
                                loaded.forEach(audioTrack -> audioTrack.setUserData(0L));
                                audioTracks.addAll(loaded);
                                loaded.forEach(audioTrack -> consumer.accept(audioTrack));
                            }
                            if(last && callback!=null)
                                callback.run();
                        }

                        @Override
                        public void noMatches() 
                        {
                            playlistLoadErrors.add(new PlaylistLoadError(index, items.get(index), "No matches found."));
                            if(last && callback!=null)
                                callback.run();
                        }

                        @Override
                        public void loadFailed(FriendlyException friendlyException)
                        {
                            playlistLoadErrors.add(new PlaylistLoadError(index, items.get(index), "Failed to load track: "+friendlyException.getLocalizedMessage()));
                            if(last && callback!=null)
                                callback.run();
                        }
                    });
                }
            }
            if(shuffle)
                shuffleTracks();
        }
        
        public void shuffleTracks()
        {
            if(audioTracks !=null)
                shuffle(audioTracks);
        }
        
        public String getName()
        {
            return name;
        }

        public List<String> getItems()
        {
            return items;
        }

        public List<AudioTrack> getAudioTracks()
        {
            return audioTracks;
        }
        
        public List<PlaylistLoadError> getPlaylistLoadErrors()
        {
            return playlistLoadErrors;
        }
    }
    
    public class PlaylistLoadError
    {
        private final int number;
        private final String item;
        private final String reason;
        
        private PlaylistLoadError(int number, String item, String reason)
        {
            this.number = number;
            this.item = item;
            this.reason = reason;
        }
        
        public int getIndex()
        {
            return number;
        }
        
        public String getItem()
        {
            return item;
        }
        
        public String getReason()
        {
            return reason;
        }
    }
}
