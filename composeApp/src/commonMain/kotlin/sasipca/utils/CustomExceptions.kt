package sasipca.utils

/**
 * Classe customizada de Exceção de dados não encontrados.
 */
class NotFoundException(message: String) : Exception(message)

/**
 * Classe customizada de Exceção de erro no repositório da API local.
 */
class RepositoryException(message: String) : Exception(message)