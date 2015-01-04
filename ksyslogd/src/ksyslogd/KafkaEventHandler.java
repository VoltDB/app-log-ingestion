/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.log4j.Logger;

import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;

public class KafkaEventHandler implements SyslogServerEventHandlerIF {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String broker;
    private Producer<String, String> kafkaProducer;
    private static Logger logger = Logger.getLogger("ksyslog");

    public static SyslogServerEventHandlerIF create(String broker) {
        return new KafkaEventHandler(broker);
    }

    private KafkaEventHandler(String broker) {
        this.broker = broker;
        initializeKafkaProducer();
    }

    private void initializeKafkaProducer() {
        try {
            Properties props = new Properties();
            props.put("metadata.broker.list", broker);
            props.put("serializer.class", "kafka.serializer.StringEncoder");
            props.put("producer.type", "async");

            ProducerConfig producerConfig = new ProducerConfig(props);
            kafkaProducer = new Producer<>(producerConfig);
        } catch (Exception ex) {
            kafkaProducer = null;
            ex.printStackTrace();
        }
    }

    @Override
    public void event(SyslogServerIF server, SyslogServerEventIF event) {
        try {
            String msg;
            Date dt;
            dt = event.getDate();
            msg = event.getMessage();
            int idx = msg.lastIndexOf('\n');
            String m = msg;
            if (idx != -1) {
                m = msg.substring(0, idx);
            }
            //escape all "
            msg = m.replaceAll("\"", "\"\"");
            //Build a CSV payload to be sent to kafka
            String tt = build(event.getLevel(), event.getFacility(), event.getHost(), dt, msg);
            send(tt, event.getHost());
            logger.info("Sending: " + tt);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Build a CSV payload to be sent to kafka
    private String build(int level, int facility, String host, Date dt, String msg) {
        String df = dateFormatter.format(dt);
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(df).append("\"").append(",")
                .append("\"").append(level).append("\"").append(",")
                .append("\"").append(facility).append("\"").append(",")
                .append("\"").append(host).append("\"").append(",")
                .append("\"").append(msg).append("\"");
        return sb.toString();
    }

    //Assume kafka partition key as host
    private void send(String message, String host) {
        try {
            if (kafkaProducer == null) {
                initializeKafkaProducer();
            }
            KeyedMessage<String, String> km
                    = new KeyedMessage<>("ksyslog", host, message);

            kafkaProducer.send(km);
        } catch (Exception ex) {
            ex.printStackTrace();
            kafkaProducer = null;
        }
    }

}
