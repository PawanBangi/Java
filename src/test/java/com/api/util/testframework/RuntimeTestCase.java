package com.api.util.testframework;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.api.util.ApiSecurity.ApiList;
import com.api.util.ApiSecurity.ApiSigning;
import com.api.util.ApiSecurity.ApiUtilException;
import com.api.util.ApiSecurity.AuthParam;
import com.api.util.ApiSecurity.FormList;
import com.api.util.ApiSecurity.SignatureMethod;
import com.api.util.testframework.dto.ExpectedResult;
import com.api.util.testframework.dto.FormData;
import com.api.util.testframework.dto.QueryString;
import com.api.util.testframework.dto.TestDatum;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class RuntimeTestCase{
	
	private static final Logger log = LogManager.getLogger(RuntimeTestCase.class);
	
	//private ApiList apiList;
	private String testName;
	private TestDatum testDatum;
	
	public RuntimeTestCase(String testName,TestDatum testDatum) {
		this.testName = testName;
		this.testDatum = testDatum;
	}
	  
    public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	/**
    * This is the actual TC, which we are expecting to test. Number of same
    * TCs with different input data will be placed in air.
     * @throws ApiUtilException 
    */
    @JUnitFactoryTest
    public void getSignatureBaseString() throws IOException, InterruptedException, ParseException {
    	  log.trace("Entering teset test application.");
    	log.info("====================> Start :: RuntimeTestCase :: getSignatureBaseString :: testName : {} ", testName);
    	
		ExpectedResult expectedResult = testDatum.getExpectedResult();
		String expectedBaseString = null;
		try {
			expectedBaseString = RuntimeTestUtility.getExpectedResultMap(expectedResult);
		} catch (ApiUtilException e1) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: getSignatureBaseString :: testName : {} ", testName);
		}
		
        String baseString = null;
		try {
			baseString = getBaseString(testDatum);
		} catch (ApiUtilException e) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: getSignatureBaseString :: testName : {} ", testName);
		}
		log.info("Run :: RuntimeTestCase :: getSignatureBaseString :: expectedBaseString : {} ", expectedBaseString);
		log.info("Run :: RuntimeTestCase :: getSignatureBaseString :: baseString : {} ", baseString);
        assertEquals(expectedBaseString, baseString);
        
        log.info("End :: RuntimeTestCase :: getSignatureBaseString :: testName : {} ", testName + "<====================");
    	
    }
    
    @JUnitFactoryTest
    public void verifyL1Signature() throws IOException, InterruptedException, ParseException {
    	log.info("==========> Start :: RuntimeTestCase :: verifyL1Signature :: testName : {} ", testName);
    	ExpectedResult expectedResult = testDatum.getExpectedResult();
		String expectedResultVerify = null;
		try {
			expectedResultVerify = RuntimeTestUtility.getExpectedResultMap(expectedResult);
		} catch (ApiUtilException e) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: verifyL1Signature :: testName : {} ", testName);
		}
		
		try {
			assertEquals(Boolean.valueOf(expectedResultVerify), ApiSigning.verifyHMACSignature(testDatum.getApiParam().getSignature(), testDatum.getApiParam().getSecret(), testDatum.getMessage()));
		} catch (ApiUtilException e) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: verifyL1Signature :: testName : {} ", testName);
		}
		
    	log.info("End :: RuntimeTestCase :: verifyL1Signature :: testName : {} ", testName + "<====================");
    }
    
    @JUnitFactoryTest
    public void verifyL2Signature() throws IOException, InterruptedException, ParseException {
    	log.info("==========> Start :: RuntimeTestCase :: verifyL2Signature :: testName : {} ", testName);
    	ExpectedResult expectedResult = testDatum.getExpectedResult();
		String expectedResultVerify = null;
		try {
			expectedResultVerify = RuntimeTestUtility.getExpectedResultMap(expectedResult);
		} catch (ApiUtilException e) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: verifyL2Signature :: testName : {} ", testName);
		}
		
		try {
			assertEquals(Boolean.valueOf(expectedResultVerify), ApiSigning.verifyRSASignature(testDatum.getMessage(), testDatum.getApiParam().getSignature(), getPublicKeyLocal("src/main/resources/test-suites/" + testDatum.getPublicKeyFileName())));
		} catch (Exception e) {
			//no errorTest parameter was used, default errorTest is true. for verifyL2Signature use expectedResult false = errorTest
			if(expectedResultVerify.equals("false")){
				log.error("ErrorTest :: RuntimeTestCase :: verifyL2Signature :: testName : {} ", testName);
//				assertEquals(expectedResultVerify, failTest);
			}else{
				log.error("Exception :: RuntimeTestCase :: verifyL2Signature :: testName : {} ", testName, e);
				fail("Not Expecting ApiUtilException error.");
			}
			
		} 
		
    	log.info("End :: RuntimeTestCase :: verifyL2Signature :: testName : {} ", testName + "<====================");
    }
    
    @JUnitFactoryTest
    public void getL1Signature() throws IOException, InterruptedException, ParseException {
    	log.info("==========> Start :: RuntimeTestCase :: getL1Signature :: testName : {} ", testName);
    	ExpectedResult expectedResult = testDatum.getExpectedResult();
		String expectedL1Signature = null;
		try {
			expectedL1Signature = RuntimeTestUtility.getExpectedResultMap(expectedResult);
		} catch (ApiUtilException e) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: getL1Signature :: testName : {} ", testName);
		}
		
		try {
			assertEquals(expectedL1Signature, ApiSigning.getHMACSignature(testDatum.getMessage(), testDatum.getApiParam().getSecret()));
		} catch (Exception e) {
			if(testDatum.getErrorTest()){
				log.error("ErrorTest :: RuntimeTestCase :: getL1Signature :: testName : {} ", testName);
				log.error("Error message: " + e.getMessage());
				assertEquals(expectedL1Signature, e.getMessage());
			}else{
				log.error("Exception :: RuntimeTestCase :: getL1Signature :: testName : {} ", testName, e);
				fail("Not Expecting ApiUtilException error.");
			}
			
		} 
		
    	log.info("End :: RuntimeTestCase :: getL1Signature :: testName : {} ", testName + "<====================");
    }
    
    @JUnitFactoryTest
    public void getL2Signature() throws IOException, InterruptedException, ParseException {
    	log.info("==========> Start :: RuntimeTestCase :: getL2Signature :: testName : {} ", testName);
    	ExpectedResult expectedResult = testDatum.getExpectedResult();
		String expectedL2Signature = null;
		try {
			expectedL2Signature = RuntimeTestUtility.getExpectedResultMap(expectedResult);
			log.info("expectedL2Signature: " + expectedL2Signature);
		} catch (ApiUtilException e) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: getL2Signature :: testName : {} ", testName);
		}
		
		try {
			assertEquals(expectedL2Signature, ApiSigning.getRSASignature(testDatum.getMessage(), ApiSigning.getPrivateKey("src/main/resources/test-suites/" + testDatum.getApiParam().getPrivateKeyFileName())));
		} catch (Exception e) {
			if(testDatum.getErrorTest()){
				log.error("ErrorTest :: RuntimeTestCase :: getL2Signature :: testName : {} ", testName);
				log.error("Error message: " + e.getMessage());
				assertEquals(expectedL2Signature, e.getMessage());
			}else{
				log.error("Exception :: RuntimeTestCase :: getL2Signature :: testName : {} ", testName, e);
				fail("Not Expecting ApiUtilException error.");
			}
			
		} 
		
    	log.info("End :: RuntimeTestCase :: getL2Signature :: testName : {} ", testName + "<====================");
    }
    
    @JUnitFactoryTest
    public void verifySupportedKeyFileType() throws IOException, InterruptedException, ParseException {
    	log.info("==========> Start :: RuntimeTestCase :: verifySupportedKeyFileType :: testName : {} ", testName);
    	ExpectedResult expectedResult = testDatum.getExpectedResult();
		String expectedResultVerify = null;
		try {
			expectedResultVerify = RuntimeTestUtility.getExpectedResultMap(expectedResult);
			log.info("expectedL2Signature: " + expectedResultVerify);
		} catch (ApiUtilException e) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: verifySupportedKeyFileType :: testName : {} ", testName);
		}
		
		try {
			if (testDatum.getApiParam().getPrivateKeyFileName() != "") {
				PrivateKey pk = ApiSigning.getPrivateKey("src/main/resources/test-suites/" + testDatum.getApiParam().getPrivateKeyFileName(), testDatum.getApiParam().getPassPhrase(), testDatum.getApiParam().getAlias());
				assertEquals(Boolean.valueOf(expectedResultVerify), Objects.equals(ApiSigning.getRSASignature(testDatum.getMessage(), pk), testDatum.getApiParam().getSignature()) );	
			}else {
				PublicKey pubk = getPublicKeyLocal("src/main/resources/test-suites/" + testDatum.getPublicKeyFileName());
				assertEquals(Boolean.valueOf(expectedResultVerify), ApiSigning.verifyRSASignature(testDatum.getMessage(), testDatum.getApiParam().getSignature(), pubk));
			}
					
		} catch (Exception e) {
			if(testDatum.getErrorTest()){
				log.error("ErrorTest :: RuntimeTestCase :: verifySupportedKeyFileType :: testName : {} ", testName);
				log.error("Error message: " + e.getMessage());
				assertEquals(expectedResultVerify, e.getMessage());
			}else{
				log.error("Exception :: RuntimeTestCase :: verifySupportedKeyFileType :: testName : {} ", testName, e);
				fail("Not Expecting ApiUtilException error.");
			}
			
		} 
		
    	log.info("End :: RuntimeTestCase :: getL2Signature :: testName : {} ", testName + "<====================");
    }
      
    @JUnitFactoryTest
    public void getSignatureToken() throws IOException, InterruptedException, ParseException {
    	log.info("====================> Start :: RuntimeTestCase :: getSignatureToken :: testName : {} ", testName);
    	
		ExpectedResult expectedResult = testDatum.getExpectedResult();
		String expectedBaseString = null;
		try {
			expectedBaseString = RuntimeTestUtility.getExpectedResultMap(expectedResult);
		} catch (ApiUtilException e1) {
			fail("Not Expecting ApiUtilException error.");
			log.error("Exception :: RuntimeTestCase :: getSignatureToken :: testName : {} ", testName);
		}
		
        String signatureToken = null;
		try {
			signatureToken = getSignatureToken(testDatum);
			log.info("Run :: RuntimeTestCase :: getSignatureToken :: expectedBaseString : {} ", expectedBaseString);
			log.info("Run :: RuntimeTestCase :: getSignatureToken :: signatureToken : {} ", signatureToken);

			assertEquals(expectedBaseString, signatureToken);
		} catch (Exception e) {
			if(testDatum.getErrorTest()){
				log.error("ErrorTest :: RuntimeTestCase :: getSignatureToken :: testName : {} ", testName);
				log.error("Error message: " + e.getMessage());
				assertEquals(expectedBaseString, e.getMessage());
			}else{
				log.error("Exception :: RuntimeTestCase :: getSignatureToken :: testName : {} ", testName, e);
				fail("Not Expecting ApiUtilException error.");
			}
			
		} 
        
        log.info("End :: RuntimeTestCase :: getSignatureToken :: testName : {} ", testName + "<====================");
    	
    }
    
    
    
    protected String getBaseString(TestDatum testDatum) throws ApiUtilException{
    	QueryString queryString = testDatum.getApiParam().getQueryString();
		ApiList apiList = null;
		ApiList queryList = null;
		String qString = "";
		if(null!=queryString){
			queryList = new ApiList();
			queryList.addAll(RuntimeTestUtility.getURLEncodedApiList(queryString.getAdditionalProperties(),true));
		}
	
		if(!testDatum.getApiParam().getSignatureUrl().isEmpty() && null!=queryString) {
			// query start with ?, replace ? with & when url already contain queryString
            if (testDatum.getApiParam().getSignatureUrl().indexOf('?') > -1) {
                qString = String.format("%s%s", "&", queryList.toString(true));
            }
           	else {
           		qString = String.format("%s%s", "?", queryList.toString(true));
           	}

		}
		
		
		FormData formData = testDatum.getApiParam().getFormData();
		if(null!=formData){
			if(null==apiList){
				apiList = new ApiList();
			}  				
			apiList.addAll(RuntimeTestUtility.getApiList(formData.getAdditionalProperties(),true));
		}
		
		
		String authPrefix = testDatum.getApiParam().getAuthPrefix();
		String signatureMethod = null;
		if(authPrefix.toLowerCase().contains("l1")){
			signatureMethod = "HMACSHA256";
		}else if(authPrefix.toLowerCase().contains("l2")){
			signatureMethod = "SHA256withRSA";
		}
        String baseString = null;
		try {
//			baseString = ApiSigning.getBaseString(
//				authPrefix,
//				signatureMethod,
//				testDatum.getApiParam().getAppId(),
//				testDatum.getApiParam().getSignatureUrl(),
//				testDatum.getApiParam().getHttpMethod(),
//				apiList,
//			    testDatum.getApiParam().getNonce(),
//			    testDatum.getApiParam().getTimestamp()
//			);
			FormList formList = new FormList();
			if (null!=apiList) {
			formList = apiList.toFormList();
			}
			baseString = ApiSigning.getBaseString(
			authPrefix,
			SignatureMethod.valueOf(signatureMethod),
			testDatum.getApiParam().getAppId(),
			URI.create(String.format("%s%s",testDatum.getApiParam().getSignatureUrl(), qString)),
			testDatum.getApiParam().getHttpMethod(),
			formList,
		    testDatum.getApiParam().getNonce(),
		    testDatum.getApiParam().getTimestamp(),
		    "1.0"
		);
			
//			baseString = ApiSigning.getBaseString(
//					
//			);
		} catch (ApiUtilException e) {
			e.printStackTrace();
			throw e;
		}
		return baseString;
    }
    
    
    protected String getSignatureToken(TestDatum testDatum) throws ApiUtilException{
    	QueryString queryString = testDatum.getApiParam().getQueryString();
    	ApiList formList = null;
		ApiList queryList = null;
		String qString = "";
		if(null!=queryString){
			queryList = new ApiList();
			
			queryList.addAll(RuntimeTestUtility.getURLEncodedApiList(queryString.getAdditionalProperties(),true));
		}
		
		FormData formData = testDatum.getApiParam().getFormData();
		if(null!=formData){
			if(null==formList){
				formList = new ApiList();
			}  		
			formList.addAll(RuntimeTestUtility.getApiList(formData.getAdditionalProperties(),true));
		}
		
		String authPrefix = testDatum.getApiParam().getAuthPrefix();
        String signatureToken = null;
		try {
			
			if(null==testDatum.getApiParam().getNonce() || testDatum.getApiParam().getNonce().isEmpty()){
				testDatum.getApiParam().setNonce("%s");
			}
			
			if(null==testDatum.getApiParam().getTimestamp() || testDatum.getApiParam().getTimestamp().isEmpty()){
				testDatum.getApiParam().setTimestamp("%s");
			}
			
			if(!testDatum.getApiParam().getSignatureUrl().isEmpty() && null!=queryString) {
				// query start with ?, replace ? with & when url already contain queryString
                if (testDatum.getApiParam().getSignatureUrl().indexOf('?') > -1) {
                    qString = String.format("%s%s", "&", queryList.toString(true));
                }
               	else {
               		qString = String.format("%s%s", "?", queryList.toString(true));
               	}

			}


//			signatureToken = ApiSigning.getSignatureToken(
//				testDatum.getApiParam().getRealm(),
//				authPrefix,
//				testDatum.getApiParam().getHttpMethod(),
//				testDatum.getApiParam().getSignatureUrl(),
//				testDatum.getApiParam().getAppId(),
//				testDatum.getApiParam().getSecret(),
//				apiList,
//				testDatum.getApiParam().getPassphrase(),
//				null,
//				"src/main/resources/test-suites/" + testDatum.getApiParam().getPrivateCertFileName(),
//			    testDatum.getApiParam().getNonce(),
//			    testDatum.getApiParam().getTimestamp()
//			);
			AuthParam authParam = new AuthParam();
			
			try {
				authParam.url = new URI(String.format("%s%s", testDatum.getApiParam().getSignatureUrl(), qString));
			}  catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			FormList apiListformData = null;
	    	if (formList !=null) {
	    		apiListformData = formList.toFormList();
	    	}
	    	
	    	
	    	
			authParam.httpMethod = testDatum.getApiParam().getHttpMethod();
			authParam.appName = testDatum.getApiParam().getAppId();
			authParam.appSecret = testDatum.getApiParam().getSecret();
			authParam.nonce = testDatum.getApiParam().getNonce();
			authParam.timestamp = testDatum.getApiParam().getTimestamp();
			authParam.formData = apiListformData;
			String filePathString = "src/main/resources/test-suites/" + testDatum.getApiParam().getPrivateKeyFileName();
			File f = new File(filePathString);
			if(f.exists() && !f.isDirectory()) { 
			    // do something
				authParam.privateKey = ApiSigning.getPrivateKey(filePathString);
			}
//			try {
			
				
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (GeneralSecurityException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			signatureToken = ApiSigning.getSignatureTokenV2(authParam).getToken();
			
			if(testDatum.getApiParam().getNonce().equals("%s") || testDatum.getApiParam().getTimestamp().equals("%s")){
				
				try{
					Pattern p = Pattern.compile("apex_l2_eg_signature=(\\S+)");
					Matcher m = p.matcher(signatureToken);
					m.find();
					String signature_value = m.group(1);
					signatureToken = signatureToken.replace(signature_value, "\"%s\"");
				}catch(Exception e){
					e.printStackTrace();
					throw e;
				}
			}
			
			
		} catch (ApiUtilException e) {
			e.printStackTrace();
			throw e;
		}
		return signatureToken;
    }
    
    private static PublicKey getPublicKeyLocal(String publicCertificateFileName) throws ApiUtilException, IOException, GeneralSecurityException {
        try {
        	if(null!=publicCertificateFileName && (publicCertificateFileName.contains(".key")||publicCertificateFileName.endsWith(".pem"))){
        		return ApiSigning.getPublicKeyPEM(publicCertificateFileName);
        	}else{
        		return ApiSigning.getPublicKeyFromX509Certificate(publicCertificateFileName);
        	}    		
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    private static PrivateKey getPrivateKeyLocal(String privateKeyFileName, String password, String alias) throws IOException, GeneralSecurityException, ApiUtilException {
        try {
            
            if(null!=privateKeyFileName && (privateKeyFileName.contains(".key")||privateKeyFileName.contains(".pem"))){
        		return ApiSigning.getPrivateKeyPEM(privateKeyFileName, password);
        	}else{
        		//For JKS file
        		return ApiSigning.getPrivateKeyFromKeyStore(privateKeyFileName, password, alias);
        	} 
        } catch (ApiUtilException e) {
            throw e;
        }
    }
    

    private static PrivateKey getPrivateKeyLocal(String privateKeyFileName) throws IOException, GeneralSecurityException, ApiUtilException {
        return ApiSigning.getPrivateKeyPEM(privateKeyFileName,"");
    }
    
    public static String combine(String... paths) {
        File file = new File(File.separator);

        for (int i = 0; i < paths.length; i++) {
            file = new File(file, paths[i]);
        }

        return file.getPath();
    }
}

