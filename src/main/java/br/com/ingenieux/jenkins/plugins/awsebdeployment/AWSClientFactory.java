package br.com.ingenieux.jenkins.plugins.awsebdeployment;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;

import org.apache.commons.lang.reflect.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class AWSClientFactory {

  private AWSCredentialsProvider creds;

  private ClientConfiguration clientConfiguration;

  private String region;

  public AWSClientFactory(AWSCredentialsProvider creds, ClientConfiguration clientConfiguration,
                          String region) {
    this.creds = creds;
    this.clientConfiguration = clientConfiguration;
    this.region = region;
  }

  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> serviceClazz)
      throws NoSuchMethodException, IllegalAccessException,
             InvocationTargetException, InstantiationException {
    T resultObj = (T) ConstructorUtils
        .invokeConstructor(serviceClazz, new Object[]{creds, clientConfiguration},
                           new Class<?>[]{AWSCredentialsProvider.class, ClientConfiguration.class});

    if (isNotBlank(region)) {
      for (ServiceEndpointFormatter formatter : ServiceEndpointFormatter.values()) {
        if (formatter.matches(resultObj)) {
          ((AmazonWebServiceClient) resultObj).setEndpoint(getEndpointFor(formatter));
          break;
        }
      }
    }

    return resultObj;
  }

  protected String getEndpointFor(ServiceEndpointFormatter formatter) {
    return String.format(formatter.serviceMask, region);
  }
}