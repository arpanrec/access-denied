/*

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2025 Arpan Mandal <me@arpanrec.com>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.

*/
package com.arpanrec.accessdenied.services;

import com.arpanrec.accessdenied.exceptions.BucketNotFoundException;
import com.arpanrec.accessdenied.models.Bucket;
import com.arpanrec.accessdenied.models.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BucketsService {

    private final BucketsRepository bucketsRepository;

    public BucketsService(@Autowired BucketsRepository bucketsRepository) {
        this.bucketsRepository = bucketsRepository;
    }

    public Bucket get(String bucketName, Namespace namespace) {
        return bucketsRepository
                .findByNameAndNamespace(bucketName, namespace)
                .orElseThrow(() -> new BucketNotFoundException(
                        "Bucket not found: " + bucketName + " in namespace: " + namespace.getName()));
    }

    public Iterable<Bucket> list(Namespace namespace) {
        return bucketsRepository.findAllByNamespace(namespace);
    }

    public Bucket create(String name, Namespace namespace) {
        Bucket bucket = new Bucket();
        bucket.setId(java.util.UUID.randomUUID().toString());
        bucket.setName(name);
        bucket.setNamespace(namespace);
        bucket.setCreatedAt(System.currentTimeMillis());
        try {
            return bucketsRepository.save(bucket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save bucket", e);
        }
    }

    public void delete(String existingName, Namespace namespace) {
        Bucket bucket = get(existingName, namespace);
        bucketsRepository.delete(bucket);
    }
}
