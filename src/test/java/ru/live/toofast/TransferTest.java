package ru.live.toofast;

import org.junit.Assert;
import org.junit.Test;
import ru.live.toofast.payment.PaymentTransferService;
import ru.live.toofast.payment.entity.Account;
import ru.live.toofast.payment.model.MoneyTransferRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TransferTest {

    //Map<Long, Account> input = Map.of(1L, new Account(1L, 1_000_000L),
    //        2L, new Account(2L, 1_000_000L));

    private Map<Long, Account> testData(){
        Map<Long, Account> input=new HashMap<>();
        input.put(1L, new Account(1L, 1_000_000L));
        input.put(2L, new Account(2L, 1_000_000L));
        return input;
    }

    @Test
    public void simpleTest(){

        Map<Long, Account> input = testData();

        PaymentTransferService service = new PaymentTransferService(input);

        service.transfer(new MoneyTransferRequest(1L, 2L, 10L));

        Assert.assertEquals(Long.valueOf(999_990), input.get(1L).getBalance());
        Assert.assertEquals(Long.valueOf(1_000_010), input.get(2L).getBalance());
    }

    @Test
    public void simpleTestSingleAccount(){

        Map<Long, Account> input = testData();

        PaymentTransferService service = new PaymentTransferService(input);

        service.transfer(new MoneyTransferRequest(1L, 1L, 10L));

        Assert.assertEquals(Long.valueOf(1_000_000), input.get(1L).getBalance());
    }


    @Test
    public void concurrentTest() throws InterruptedException, ExecutionException {

        Map<Long, Account> input = testData();

        PaymentTransferService service = new PaymentTransferService(input);

        MoneyTransferRequest first = new MoneyTransferRequest(1L, 2L, 1L);
        MoneyTransferRequest second = new MoneyTransferRequest(2L, 1L, 2L);

        List<MoneyTransferTask> taskList = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            taskList.add(new MoneyTransferTask(service, first));
            taskList.add(new MoneyTransferTask(service, second));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(20);

        List<Future<Void>> futures = executorService.invokeAll(taskList);

        for (Future<Void> future : futures) {
            future.get();
        }


        Assert.assertEquals(Long.valueOf(1100000), input.get(1L).getBalance());
        Assert.assertEquals(Long.valueOf(900000), input.get(2L).getBalance());
    }




    private class MoneyTransferTask implements Callable<Void> {
        private PaymentTransferService service;
        private MoneyTransferRequest moneyTransferRequest;

        public MoneyTransferTask(PaymentTransferService service, MoneyTransferRequest moneyTransferRequest) {
            this.service = service;
            this.moneyTransferRequest = moneyTransferRequest;
        }

        @Override
        public Void call() {
            service.transfer(moneyTransferRequest);
            return null;
        }
    }





}
