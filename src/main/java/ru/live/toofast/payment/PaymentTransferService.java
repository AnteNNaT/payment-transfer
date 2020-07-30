package ru.live.toofast.payment;

import ru.live.toofast.payment.entity.Account;
import ru.live.toofast.payment.model.MoneyTransferRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PaymentTransferService {
    
    private Map<Long, Account> accounts;

    private  Map<Long, Lock> lockMap=new ConcurrentHashMap<>();

    public PaymentTransferService(Map<Long, Account> accounts) {
        this.accounts = accounts;
    }

    public void transfer(MoneyTransferRequest request){

        
       Account fromAccount = accounts.get(request.getFrom());
       Account toAccount = accounts.get(request.getTo());

        Account first;
        Account second;


        if(fromAccount.getId() > toAccount.getId()){
            first = toAccount;
            second = fromAccount;
        } else {
            first = fromAccount;
            second = toAccount;
        }

       Lock firstLock = lockMap.computeIfAbsent(first.getId(), k  -> new ReentrantLock());
       Lock secondLock = lockMap.computeIfAbsent(second.getId(), k  -> new ReentrantLock());

        try {
            firstLock.lock();
            secondLock.lock();

            Long amount = request.getAmount();
            if (fromAccount.getBalance() < amount) {
                throw new RuntimeException("Not enough funds");
            }

            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);
        } finally {
            firstLock.unlock();
            secondLock.unlock();
        }

    }
    
    
}
