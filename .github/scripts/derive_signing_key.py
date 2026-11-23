#!/usr/bin/env python3
"""Derives the release signing key of this fork from a secret, and writes it as a PKCS#12 store.

The same secret always gives the same key and the same certificate (deterministic ECDSA, fixed
validity and serial), so every build is signed identically and installs as an update. The secret
goes through scrypt, which makes guessing it from the public certificate slow.

Usage: SIGNING_SECRET=... derive_signing_key.py OUTPUT_FILE ALIAS
The store password is the secret itself.
"""

import datetime
import hashlib
import os
import sys

from cryptography import x509
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.serialization import BestAvailableEncryption, pkcs12
from cryptography.x509.oid import NameOID

SALT = b"tonival94/MaterialFiles release signing key v1"
# Order of the NIST P-256 group.
P256_ORDER = 0xFFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551


def main() -> None:
    output_file, alias = sys.argv[1], sys.argv[2]
    secret = os.environ.get("SIGNING_SECRET", "")
    if len(secret) < 12:
        sys.exit("SIGNING_SECRET is missing or shorter than 12 characters")

    seed = hashlib.scrypt(
        secret.encode(), salt=SALT, n=2**17, r=8, p=1, maxmem=256 * 1024 * 1024, dklen=48
    )
    private_value = int.from_bytes(seed, "big") % (P256_ORDER - 1) + 1
    key = ec.derive_private_key(private_value, ec.SECP256R1())

    name = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "tonival94 Material Files")])
    utc = datetime.timezone.utc
    certificate = (
        x509.CertificateBuilder()
        .subject_name(name)
        .issuer_name(name)
        .public_key(key.public_key())
        .serial_number(1)
        .not_valid_before(datetime.datetime(2026, 1, 1, tzinfo=utc))
        .not_valid_after(datetime.datetime(2126, 1, 1, tzinfo=utc))
        .sign(key, hashes.SHA256(), ecdsa_deterministic=True)
    )

    with open(output_file, "wb") as file:
        file.write(
            pkcs12.serialize_key_and_certificates(
                alias.encode(), key, certificate, None,
                BestAvailableEncryption(secret.encode()),
            )
        )
    fingerprint = certificate.fingerprint(hashes.SHA256()).hex(":").upper()
    print(f"Signing certificate SHA-256: {fingerprint}")


if __name__ == "__main__":
    main()
