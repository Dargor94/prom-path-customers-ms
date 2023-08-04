package org.dargor.customer.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.h2.tools.Server;

import java.sql.SQLException;

@Configuration
public class DataBaseConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2Server1() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9010");
    }

}
