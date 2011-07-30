package h2weibo;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.S3Service;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * @author Rakuraku Jyo
 */
public class S3BackupTask extends DBTask {
    private static final Logger log = Logger.getLogger(S3BackupTask.class.getName());

    public void run() {
        String dump = getHelper().dump();

        try {
            AWSCredentials awsCredentials = new AWSCredentials("07FDF4N8HAEG3G3W1WG2", "wjeo/z5LrRyWpXCpOPs0lLpn49R1gq/85QbtbW9k");
            S3Service s3Service = new RestS3Service(awsCredentials);
            S3Bucket backup = s3Service.getBucket("h2weibo.backup");

            String dateString = DateFormatUtils.format(new Date(), "yyyy-MM-dd_HH");
            S3Object object = new S3Object(dateString + ".json", dump);
            object = s3Service.putObject(backup, object);
            System.out.println("S3Object after upload: " + object);
        } catch (Exception e) {
            log.error("Failed to upload to S3.");
            log.error(e);
        }
    }

    public void restore(String from) {
        try {
            AWSCredentials awsCredentials = new AWSCredentials("07FDF4N8HAEG3G3W1WG2", "wjeo/z5LrRyWpXCpOPs0lLpn49R1gq/85QbtbW9k");
            S3Service s3Service = new RestS3Service(awsCredentials);

            S3Object object = s3Service.getObject("h2weibo.backup", from + ".json");
            InputStream inputStream = object.getDataInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            String data = "";
            while ((line = reader.readLine()) != null) {
                data += line;
            }
            reader.close();

            getHelper().restore(data);
        } catch (Exception e) {
            log.error("Failed to restore from S3.");
            log.error(e);
        }
    }
}
