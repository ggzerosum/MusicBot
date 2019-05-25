/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands;

import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;

import net.dv8tion.jda.core.Permission;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public abstract class AdminCommand<T> extends Command
{
    protected String noneArgsInEventMessage;
    protected String nullArgsInEventMessage;
    protected String emptyListMessage;
    protected List<T> list;
    
    public AdminCommand()
    {
        this.category = new Category("Admin", event -> 
        {
            if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if(event.getGuild()==null)
                return true;
            return event.getMember().hasPermission(Permission.MANAGE_SERVER);
        });
        this.guildOnly = true;
    }
    
    protected void execute(CommandEvent event) 
    {
    	CommandClient commandClient = event.getClient();
    	
        if(event.getArgs().isEmpty())
        {
            event.reply(commandClient.getError() + nullArgsInEventMessage);
            return;
        }
        Settings settings = commandClient.getSettingsFor(event.getGuild());
        
        if(event.getArgs().equalsIgnoreCase("none"))
        {
        	setSettingsTonull(settings);
        	event.reply(commandClient.getSuccess() + noneArgsInEventMessage);
        }
        else
        {
        	setList(event);
        	switch(list.size()) {
        	case 0:
        		event.reply(commandClient.getWarning() + emptyListMessage + event.getArgs()+"\"");
        		break;
        	case 1:
        		setSettingsToListEntry(settings);
                event.reply(commandClient.getSuccess() + executeSuccessMessage());
                break;
            default:
            	event.reply(commandClient.getWarning() + 
            			FormatUtil.listOfChannels(list, multiChannelsHeader(event.getArgs()), multiChannelsBody(event)));
            	break;
        	}
        }
    }

    protected abstract void setSettingsTonull(Settings settings);
    protected abstract void setList(CommandEvent event);
    protected abstract void setSettingsToListEntry(Settings settings);
    protected abstract String executeSuccessMessage();
    protected abstract String multiChannelsHeader(String eventArgs);
    protected abstract String[] multiChannelsBody(CommandEvent event);
}
