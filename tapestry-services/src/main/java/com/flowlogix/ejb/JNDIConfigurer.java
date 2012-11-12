/*
 * Copyright 2012 lprimak.
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
package com.flowlogix.ejb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.StringUtils;

/**
 *
 * @author lprimak
 */
@Slf4j
public class JNDIConfigurer
{
    private final Map<String, Config> _conf = new HashMap<>();
    private @Getter final Map<String, Config> configuration = Collections.synchronizedMap(_conf);
    private @Getter static JNDIConfigurer instance = new JNDIConfigurer();
    
    
    public JNDIObjectLocator buildLocator(String mappedName) throws NamingException
    {
        if(StringUtils.hasText(mappedName) == false)
        {
            return new JNDIObjectLocator();
        }
        
        Config config = getConfiguration().get(mappedName);
        if(config == null)
        {
            log.error("Building EJB Locator Failed: mappedName %s is not configured", mappedName);
            return new JNDIObjectLocator();
        }
        
        Hashtable<String, String> env = new Hashtable<>();
        if(StringUtils.hasText(config.getHostname()))
        {
            env.put("org.omg.CORBA.ORBInitialHost", config.getHostname());           
        }
        if(config.getPort() != null)
        {
            env.put("org.omg.CORBA.ORBInitialPort", config.getPort().toString());           
        }
        for(Map.Entry<String, String> entry : config.getAdditionalProperties().entrySet())
        {
            env.put(entry.getKey(), entry.getValue());
        }
        JNDIObjectLocator locator;
        if(env.isEmpty())
        {
            locator = new JNDIObjectLocator();
        }
        else
        {
            locator = new JNDIObjectLocator(new InitialContext(env));
        }
        if(StringUtils.hasText(config.getPrefix()))
        {
            locator.setPortableNamePrefix(config.getPrefix());
        }
        locator.setNoCaching(!config.isCache());
        return locator;
    }
    
    
    public JNDIObjectLocator buildLocator() throws NamingException
    {
        return buildLocator(null);
    }

    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class Config
    {
        public Config(String hostname, Integer port, String prefix)
        {
            this(hostname, port, prefix, false);
        }
        
        
        String hostname;
        Integer port;
        String prefix;
        boolean cache;
        final Map<String, String> additionalProperties = new HashMap<>();
    }
}