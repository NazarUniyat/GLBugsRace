package com.basecamp.service.impl;

import com.basecamp.exception.InternalException;
import com.basecamp.exception.InvalidDataException;
import com.basecamp.service.ProductService;
import com.basecamp.wire.GetHandleProductIdsResponse;
import com.basecamp.wire.GetProductInfoResponse;
import com.basecamp.wire.ThreadInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ConcurrentTaskService taskService;

    public GetProductInfoResponse getProductInfo(String productId) {

        validateId(productId);

        log.info("Product id {} was successfully validated.", productId);

        return callToDbAnotherServiceETC(productId);
    }

    public GetHandleProductIdsResponse handleProducts(List<String> productIds) {
        Map<String, Future<String>> handledTasks = new HashMap<>();
        productIds.forEach(productId ->
                handledTasks.put(
                        productId,
                        taskService.handleProductIdByExecutor(productId)));

        List<String> handledIds = handledTasks.entrySet().stream().map(stringFutureEntry -> {
            try {
                return stringFutureEntry.getValue().get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(stringFutureEntry.getKey() + " execution error!");
            }

            return stringFutureEntry.getKey() + " is not handled!";
        }).collect(Collectors.toList());

        return GetHandleProductIdsResponse.builder()
                .productIds(handledIds)
                .build();
    }

    public void stopProductExecutor() {
        log.warn("Calling to stop product executor...");

        taskService.stopExecutorService();

        log.info("Product executor stopped.");
    }


    @Override
    public TreeSet<ThreadInfo> homework(int countOfThread, int finish) throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newFixedThreadPool(countOfThread);

        Callable<ThreadInfo> callable = () -> {
            String name;
            int i = 0;
            long start = System.nanoTime();
            while (i < finish) {
                try {
                    Thread.sleep(1000);
                    i += (int) (Math.random() * 5);
                    System.out.println("'" + currentThread().getName() + "' on - " + i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long duration = System.nanoTime() - start;
            name = currentThread().getName();
            return ThreadInfo.builder().name(name).duration(duration).finished(System.nanoTime()).build();
        };


        List<Callable<ThreadInfo>> callables = new ArrayList<>();
        for (int i = 0; i < countOfThread; i++) {
            callables.add(callable);
        }
        List<Future<ThreadInfo>> futures = executorService.invokeAll(callables);

//      Sorted by duration(loop working)
//      As a result there can be the difference between the representation in console and TreeSet
        TreeSet<ThreadInfo> threadInfos = new TreeSet<>();
        for (Future<ThreadInfo> future : futures) {
            threadInfos.add(future.get());
        }

        executorService.shutdown();
        return threadInfos;
    }


    private void validateId(String id) {

        if (StringUtils.isEmpty(id)) {
            // all messages could be moved to messages properties file (resources)
            String msg = "ProductId is not set.";
            log.error(msg);
            throw new InvalidDataException(msg);
        }

        try {
            Integer.valueOf(id);
        } catch (NumberFormatException e) {
            String msg = String.format("ProductId %s is not a number.", id);
            log.error(msg);
            throw new InvalidDataException(msg);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalException(e.getMessage());
        }
    }


    private GetProductInfoResponse callToDbAnotherServiceETC(String productId) {
        return GetProductInfoResponse.builder()
                .id(productId)
                .name("ProductName")
                .status("ProductStatus")
                .build();
    }

}
