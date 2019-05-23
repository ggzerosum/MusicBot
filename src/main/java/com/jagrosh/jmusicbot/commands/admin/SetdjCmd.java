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
package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;

import net.dv8tion.jda.core.entities.Role;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetdjCmd extends AdminCommand<Role>
{
    public SetdjCmd()
    {
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.arguments = "<rolename|NONE>";
        this.nullArgsInEventMessage = " Please include a role name or NONE";
        this.noneArgsInEventMessage = " DJ role cleared; Only Admins can use the DJ commands.";
        this.emptyListMessage = " No Roles found matching \"";
    }
    
    protected void setSettingsTonull(Settings settings) {
    	settings.setDJRole(null);
    }
    
    protected void setList(CommandEvent event) {
    	this.list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
    }
    
    protected void setSettingsToListEntry(Settings settings) {
    	settings.setDJRole(list.get(0));
    }
    
    protected String executeSuccessMessage() {
    	return " DJ commands can now be used by users with the **"+list.get(0).getName()+"** role.";
    }
    
    protected String multiChannelsHeader(String eventArgs) {
    	return " Multiple text channels found matching \""+eventArgs+"\":";
    }
    
    protected String[] multiChannelsBody(CommandEvent event) {
    	String temp[] = {};
    	
    	for(int i=0; i<6 && i<list.size(); i++)
    		temp[i] = "\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
    	
    	return temp;
    }
}
