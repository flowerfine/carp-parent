package cn.sliew.carp.framework.storage.config;

import cn.sliew.flink.platform.config.blobstorage.BlobStorageCredentials;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class HdfsFileSystemFactory {

    static final String CORE_SITE_XML = "core-site.xml";
    static final String HDFS_SITE_XML = "hdfs-site.xml";

    private static final String HDFS_PROVIDER = "hdfs";

    static final String KERBEROS_CONF = "krb5.conf";
    static final String KERBEROS_KEYTAB = "keytab";
    static final String KERBEROS_KEYTAB_PRINCIPAL = "keytab-principal";
    private static final String SYS_PROP_JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";

    public FileSystem createClient(String hadoopConfDir) {
        Configuration configuration;
        if (StringUtils.isBlank(hadoopConfDir)) {
            configuration = new Configuration();
            log.info("Using default configuration for Hdfs client");
        } else {
            log.info("Using local configuration files from {} for Hdfs client", hadoopConfDir);
            configuration = createHadoopConfiguration(
                    Path.of(hadoopConfDir, CORE_SITE_XML),
                    Path.of(hadoopConfDir, HDFS_SITE_XML));
            configureKerberos(configuration,
                    Path.of(hadoopConfDir, KERBEROS_KEYTAB),
                    Path.of(hadoopConfDir, KERBEROS_CONF),
                    readKeytabPrincipalFromFile(Path.of(hadoopConfDir, KERBEROS_KEYTAB_PRINCIPAL)));
        }

        return this.newClient(configuration);
    }

    public FileSystem createClient(BlobStorageCredentials blobStorageCredentials) {
        Path coreSiteXml = blobStorageCredentials.getCredentialPath(HDFS_PROVIDER, CORE_SITE_XML)
                .orElseThrow(() -> new IllegalArgumentException("Hdfs core-site.xml not configured"));
        Path hdfsSiteXml = blobStorageCredentials.getCredentialPath(HDFS_PROVIDER, HDFS_SITE_XML)
                .orElseThrow(() -> new IllegalArgumentException("Hdfs hdfs-site.xml not configured"));
        Configuration configuration = createHadoopConfiguration(coreSiteXml, hdfsSiteXml);
        configureKerberos(configuration,
                blobStorageCredentials.getCredentialPath(HDFS_PROVIDER, KERBEROS_KEYTAB).orElse(Path.of("")),
                blobStorageCredentials.getCredentialPath(HDFS_PROVIDER, KERBEROS_CONF).orElse(Path.of("")),
                blobStorageCredentials.getCredentialObject(HDFS_PROVIDER, KERBEROS_KEYTAB_PRINCIPAL).orElse(null));
        return this.newClient(configuration);
    }

    private FileSystem newClient(Configuration configuration) {
        try {
            return FileSystem.get(configuration);
        } catch (IOException e) {
            log.error("Failed to init hdfs filesystem", e);
            throw new HdfsArtifactStorageRuntimeException(e);
        }
    }

    @VisibleForTesting
    static Configuration createHadoopConfiguration(Path coreSiteXml, Path hdfsSiteXml) {
        Configuration configuration = new Configuration();
        if (coreSiteXml.toFile().exists()) {
            configuration.addResource(new org.apache.hadoop.fs.Path(coreSiteXml.toString()));
        }

        if (hdfsSiteXml.toFile().exists()) {
            configuration.addResource(new org.apache.hadoop.fs.Path(hdfsSiteXml.toString()));
        }

        return configuration;
    }

    @VisibleForTesting
    static boolean isKerberosEnabled(Path keytab, Path krb5Conf, @Nullable String keytabPrincipal) {
        return keytab.toFile().exists() && krb5Conf.toFile().exists() && StringUtils.isNotBlank(keytabPrincipal);
    }

    @VisibleForTesting
    static void configureKerberos(Configuration configuration, Path keytab, Path krb5Conf, @Nullable String keyTabPrincipal) {
        if (!isKerberosEnabled(keytab, krb5Conf, keyTabPrincipal)) {
            log.info("Kerberos is not enabled");
        } else {
            log.info("Kerberos is enabled");
            System.setProperty(SYS_PROP_JAVA_SECURITY_KRB5_CONF, krb5Conf.toString());
            UserGroupInformation.setConfiguration(configuration);

            try {
                UserGroupInformation.loginUserFromKeytab(keyTabPrincipal, keytab.toString());
            } catch (IOException e) {
                log.error("Failed to login hdfs from keytab", e);
                throw new HdfsArtifactStorageRuntimeException(e);
            }
        }
    }

    @Nullable
    @VisibleForTesting
    static String readKeytabPrincipalFromFile(Path keytabPrincipal) {
        File file = keytabPrincipal.toFile();
        if (!file.exists()) {
            return null;
        } else {
            try {
                return Files.readString(file.toPath()).trim();
            } catch (IOException e) {
                log.error("Failed to read keytab principal from file {}", keytabPrincipal);
                throw new HdfsArtifactStorageRuntimeException(e);
            }
        }
    }
}
