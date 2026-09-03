package br.com.fiap.sus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic(systemName = "Plataforma Digital de Atendimento SUS")
@SpringBootApplication
public class SusApplication {

    public static void main(String[] args) {
        SpringApplication.run(SusApplication.class, args);
    }
}
