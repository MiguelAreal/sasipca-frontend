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
        val url = buildString {
            append("${ApiConfig.baseUrl()}/deliveries")
            query?.let {
                append("?StatusId=${it.statusId}&BeneficiaryId=${it.beneficiaryId}&DateFrom=${it.dateFrom}&DateTo=${it.dateTo}")
            }
        }

        return client.requestWithAuth(
            method = HttpMethod.Get,
            url = url
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
            url = "${ApiConfig.baseUrl()}/stock/deliveries",
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
            url = "${ApiConfig.baseUrl()}/stock/deliveries/$deliveryId",
            body = body
        )
    }

    /**
     * Elimina uma entrega existente (Apenas se tiver 'Agendada')
     */
    suspend fun deleteDelivery(deliveryId: Int): Resposta {
        return client.requestWithAuth(
            method = HttpMethod.Delete,
            url = "${ApiConfig.baseUrl()}/stock/deliveries/$deliveryId"
        )
    }
}
