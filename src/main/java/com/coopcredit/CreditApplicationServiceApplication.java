import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(excludeName = {
		"org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration",
		"org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration",
		"org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration",
		"org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration",
		"org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration",
		"org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration",
		"org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration",
		"org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration",
		"org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration",
		"org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration"
})
@EnableJpaRepositories(basePackages = "com.coopcredit.infrastructure.persistence.repository")
@EntityScan(basePackages = "com.coopcredit.infrastructure.persistence.entity")
public class CreditApplicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditApplicationServiceApplication.class, args);
	}

}
