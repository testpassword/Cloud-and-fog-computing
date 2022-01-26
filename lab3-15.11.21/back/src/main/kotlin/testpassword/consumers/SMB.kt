package testpassword.consumers

import com.microsoft.azure.storage.CloudStorageAccount

object SMB {

    private fun parseCreds(creds: String): String {
        val (address, key) = creds.split(";")
        val name = address.split(".").first().split("//")[1]
        return "DefaultEndpointsProtocol=https;AccountName=${name};AccountKey=${key}"
    }

    operator fun invoke(creds: String, product: ByteArray, productName: String) =
        CloudStorageAccount
            .parse(parseCreds(creds))
            .createCloudFileClient()
            .getShareReference("optreports")
            .also { it.createIfNotExists() }
            .rootDirectoryReference
            .getFileReference(productName)
            .uploadFromByteArray(product, 0, product.size)
}