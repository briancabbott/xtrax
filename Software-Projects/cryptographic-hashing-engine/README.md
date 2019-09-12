

A component (service?) that performs a battery of hashing services on a target url.


Hash a given resource (target data-set, at URL), at/on date, write crypto-ledger on op-completion,
return results to calling service as well to optionally write the digests into the dataset.
   - One question/exploration to resolve: if we write the hash digest into the data itself, it changes the computed hash however,
     it would be better to do this then to write it as seperate files. I want the digests to be embedded, to live with the dataset in
     a way.




Algorithms to support:

Cryptographic hash algorithms
There are many cryptographic hash algorithms; this section lists a few algorithms that are referenced relatively often. A more extensive list can be found on the page containing a comparison of cryptographic hash functions.

MD5
Main article: MD5
MD5 was designed by Ronald Rivest in 1991 to replace an earlier hash function MD4, and was specified in 1992 as RFC 1321. Collisions against MD5 can be calculated within seconds which makes the algorithm unsuitable for most use cases where a cryptographic hash is required. MD5 produces a digest of 128 bits (16 bytes).

SHA-1
Main article: SHA-1
SHA-1 was developed as part of the U.S. Government's Capstone project. The original specification – now commonly called SHA-0 – of the algorithm was published in 1993 under the title Secure Hash Standard, FIPS PUB 180, by U.S. government standards agency NIST (National Institute of Standards and Technology). It was withdrawn by the NSA shortly after publication and was superseded by the revised version, published in 1995 in FIPS PUB 180-1 and commonly designated SHA-1. Collisions against the full SHA-1 algorithm can be produced using the shattered attack and the hash function should be considered broken. SHA-1 produces a hash digest of 160 bits (20 bytes).

Documents may refer to SHA-1 as just "SHA", even though this may conflict with the other Standard Hash Algorithms
such as SHA-0, SHA-2 and SHA-3.

RIPEMD-160
Main article: RIPEMD-160
RIPEMD (RACE Integrity Primitives Evaluation Message Digest) is a family of cryptographic hash functions developed in Leuven, Belgium, by Hans Dobbertin, Antoon Bosselaers and Bart Preneel at the COSIC research group at the Katholieke Universiteit Leuven, and first published in 1996. RIPEMD was based upon the design principles used in MD4, and is similar in performance to the more popular SHA-1. RIPEMD-160 has however not been broken. As the name implies, RIPEMD-160 produces a hash digest of 160 bits (20 bytes).

bcrypt
Main article: Bcrypt
bcrypt is a password hashing function designed by Niels Provos and David Mazières, based on the Blowfish cipher, and presented at USENIX in 1999. Besides incorporating a salt to protect against rainbow table attacks, bcrypt is an adaptive function: over time, the iteration count can be increased to make it slower, so it remains resistant to brute-force search attacks even with increasing computation power.

Whirlpool
Main article: Whirlpool (cryptography)
In computer science and cryptography, Whirlpool is a cryptographic hash function. It was designed by Vincent Rijmen and Paulo S. L. M. Barreto, who first described it in 2000. Whirlpool is based on a substantially modified version of the Advanced Encryption Standard (AES). Whirlpool produces a hash digest of 512 bits (64 bytes).

SHA-2
Main article: SHA-2
SHA-2 (Secure Hash Algorithm 2) is a set of cryptographic hash functions designed by the United States National Security Agency (NSA), first published in 2001. They are built using the Merkle–Damgård structure, from a one-way compression function itself built using the Davies–Meyer structure from a (classified) specialized block cipher.

SHA-2 basically consists of two hash algorithms: SHA-256 and SHA-512. SHA-224 is a variant of SHA-256 with different starting values and truncated output. SHA-384 and the lesser known SHA-512/224 and SHA-512/256 are all variants of SHA-512. SHA-512 is more secure than SHA-256 and is commonly faster than SHA-256 on 64 bit machines such as AMD64.

The output size in bits is given by the extension to the "SHA" name, so SHA-224 has an output size of 224 bits (28 bytes), SHA-256 produces 32 bytes, SHA-384 produces 48 bytes and finally SHA-512 produces 64 bytes.

SHA-3
Main article: SHA-3
SHA-3 (Secure Hash Algorithm 3) was released by NIST on August 5, 2015. SHA-3 is a subset of the broader cryptographic primitive family Keccak. The Keccak algorithm is the work of Guido Bertoni, Joan Daemen, Michael Peeters, and Gilles Van Assche. Keccak is based on a sponge construction which can also be used to build other cryptographic primitives such as a stream cipher. SHA-3 provides the same output sizes as SHA-2: 224, 256, 384 and 512 bits.

Configurable output sizes can also be obtained using the SHAKE-128 and SHAKE-256 functions. Here the -128 and -256 extensions to the name imply the security strength of the function rather than the output size in bits.

BLAKE2
Main article: BLAKE2
An improved version of BLAKE called BLAKE2 was announced in December 21, 2012. It was created by Jean-Philippe Aumasson, Samuel Neves, Zooko Wilcox-O'Hearn, and Christian Winnerlein with the goal to replace widely used, but broken MD5 and SHA-1 algorithms. When run on 64-bit x64 and ARM architectures, BLAKE2b is faster than SHA-3, SHA-2, SHA-1, and MD5. Although BLAKE nor BLAKE2 have not been standardized as SHA-3 it has been used in many protocols including the Argon2 password hash for the high efficiency that it offers on modern CPUs. As BLAKE was a candidate for SHA-3, BLAKE and BLAKE2 both offer the same output sizes as SHA-3 – including a configurable output size.
