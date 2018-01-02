/* This file is part of VoltDB.
 * Copyright (C) 2008-2018 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package ksyslogd;

import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;

public class Ksyslogd {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: Ksyslogd kafka-broker-url [listener-ip]");
            System.exit(1);
        }
        SyslogServerEventHandlerIF kafkaEventHandler = KafkaEventHandler.create(args[0]);
        SyslogServerIF syslogServer = SyslogServer.getThreadedInstance("udp");

        SyslogServerConfigIF syslogServerConfig = syslogServer.getConfig();
        if (args.length > 1) {
            syslogServerConfig.setHost(args[1]);
        }
        syslogServerConfig.setPort(1514);

        syslogServerConfig.addEventHandler(kafkaEventHandler);
        while (true) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                // nothing!
            }
        }

    }

}
