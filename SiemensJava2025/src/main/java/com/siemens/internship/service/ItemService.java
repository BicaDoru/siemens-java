package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     * ---------------------
     * Asynchronously processes all items using a separate task per item ID.
     * Each task fetches the item, marks it as "PROCESSED", saves it, and returns the updated item.
     * All tasks are submitted to the executor and combined using CompletableFuture.allOf().
     * The method returns a CompletableFuture that completes when all tasks finish,
     * containing the list of successfully processed items or throwing an exception if any task fails.
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        // process each item asynchronously and return a list of futures with results
        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100); // Simulate delay
                        Optional<Item> optionalItem = itemRepository.findById(id);
                        if (optionalItem.isPresent()) {
                            Item item = optionalItem.get();
                            item.setStatus("PROCESSED");
                            return itemRepository.save(item); // save and return updated item
                        } else {
                            throw new IllegalArgumentException("Item with ID " + id + " not found");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException("Thread interrupted for item ID " + id, e);
                    } catch (Exception e) {
                        throw new CompletionException("Failed to process item with ID " + id, e);
                    }
                }, executor))
                .collect(Collectors.toList());

        // combine all futures and collect results when done
        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // compose and transform to a single future with list of processed items
        return allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join) // join because all futures are completed
                        .collect(Collectors.toList())
        );
    }

}