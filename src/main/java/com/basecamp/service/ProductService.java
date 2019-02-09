package com.basecamp.service;

import com.basecamp.wire.GetHandleProductIdsResponse;
import com.basecamp.wire.GetProductInfoResponse;
import com.basecamp.wire.ThreadInfo;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public interface ProductService {

    GetProductInfoResponse getProductInfo(String productId);

    GetHandleProductIdsResponse handleProducts(List<String> productIds);

    void stopProductExecutor();

    TreeSet<ThreadInfo> homework(int countOfThread, int finish) throws InterruptedException, ExecutionException;

}
