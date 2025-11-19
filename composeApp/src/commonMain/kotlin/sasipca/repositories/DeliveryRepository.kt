package sasipca.repositories

import sasipca.models.*
import sasipca.storage.ApiConfig
import sasipca.storage.requestWithAuth
import io.ktor.client.*
import io.ktor.http.*

class DeliveryRepository(private val client: HttpClient) {

    /**
     * Consulta entregas com filtros opcionais
     */
    suspend fun getDeliveries(query: DeliveryGet? = null): List<Delivery> {
        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("deliveries")

                query?.let {
                    it.statusId?.let { value ->
                        parameters.append("StatusId", value.toString())
                    }
                    it.beneficiaryId?.let { value ->
                        parameters.append("BeneficiaryId", value.toString())
                    }
                    it.dateFrom?.let { value ->
                        parameters.append("DateFrom", value)
                    }
                    it.dateTo?.let { value ->
                        parameters.append("DateTo", value)
                    }
                }
            }.buildString()
        )
    }



    /**
     * Agenda uma nova entrega para uma data específica.
     *
     * Cria uma entrega imediata.
     */
    suspend fun scheduleDelivery(body: DeliveryPost, instant: Boolean): Delivery {
        return client.requestWithAuth(
            method = HttpMethod.Post,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stock","deliveries")
                parameters.append("instant", instant.toString())

            }.buildString(),
            body = body
        )
    }

    /**
     * Atualiza uma entrega existente (ex: data, status, itens)
     * Apenas se estiver 'Agendada'
     */
    suspend fun putDelivery(deliveryId: Int, body: DeliveryPut): Delivery {
        return client.requestWithAuth(
            method = HttpMethod.Put,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stock", "deliveries", deliveryId.toString())
            }.buildString(),
            body = body
        )
    }

    /**
     * Elimina uma entrega existente (Apenas se tiver 'Agendada')
     */
    suspend fun deleteDelivery(deliveryId: Int): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Delete,
            url = URLBuilder(ApiConfig.baseUrl()).apply {
                appendPathSegments("stock", "deliveries", deliveryId.toString())
            }.buildString(),
        )
    }
}
