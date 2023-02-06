package com.coresky.web;

public class Aaaa {

    public static void main(String[] args) {
//        Web3j web3j = Web3j.build(new HttpService("https://goerli.infura.io/v3/e0ec62bcaf8c48f280127c0aa347ca24"));
//        String methodName = "staticCall";
//        List<Type> inputParameters = new ArrayList<>();
//        Address address = new Address("0xC35b21166eDC2B29d273223B3cD15d19617238F2");
//        DynamicBytes calldata = new DynamicBytes("23b872dd000000000000000000000000ffc1876d44aec1a89acf00a0f3cda1375a9b6bfb00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001".getBytes());
//        DynamicBytes extradata = new DynamicBytes("00000000000000000000000000000000".getBytes());
//        inputParameters.add(address);
//        inputParameters.add(calldata);
//        inputParameters.add(extradata);
//
//        List<TypeReference<?>> outputParameters = new ArrayList<>();
//        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
//        };
//        outputParameters.add(typeReference);
//
//        Function function = new Function(methodName, inputParameters, outputParameters);
//        String data = FunctionEncoder.encode(function);
//        org.web3j.protocol.core.methods.request.Transaction transaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(Web3Constant.EMPTY_ADDRESS, "0xC126888d5Af2000bdaC88EC5cA44a9cc8b397D04", data);
//
//        EthCall ethCall;
//        try {
//            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
//            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
//            System.out.println(results.get(0).getValue().toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
