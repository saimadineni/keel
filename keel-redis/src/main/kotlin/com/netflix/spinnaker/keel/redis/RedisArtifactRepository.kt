package com.netflix.spinnaker.keel.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.netflix.spinnaker.keel.api.ArtifactType
import com.netflix.spinnaker.keel.api.DeliveryArtifact
import com.netflix.spinnaker.keel.api.DeliveryArtifactVersion
import com.netflix.spinnaker.keel.persistence.ArtifactRepository
import com.netflix.spinnaker.kork.jedis.RedisClientDelegate
import redis.clients.jedis.JedisCommands

class RedisArtifactRepository(
  private val redisClient: RedisClientDelegate,
  private val objectMapper: ObjectMapper
) : ArtifactRepository {
  override fun store(artifact: DeliveryArtifact) {
    redisClient.withCommandsClient<Unit> { redis ->
      redis.sadd("keel.delivery_artifacts", artifact.asJson())
    }
  }

  override fun store(artifactVersion: DeliveryArtifactVersion) {
    redisClient.withCommandsClient<Unit> { redis ->
      with(artifactVersion) {
        require(redis.isRegistered(artifact)) {
          "No registered artifact with name ${artifact.name} and type ${artifact.type}"
        }
        redis.sadd(
          artifact.versionsKey,
          objectMapper.writeValueAsString(this)
        )
      }
    }
  }

  override fun get(name: String, type: ArtifactType): DeliveryArtifact? =
    redisClient.withCommandsClient<DeliveryArtifact?> { redis ->
      DeliveryArtifact(name, type)
        .let {
          if (redis.isRegistered(it)) it else null
        }
    }

  override fun versions(artifact: DeliveryArtifact): List<DeliveryArtifactVersion> =
    redisClient.withCommandsClient<List<DeliveryArtifactVersion>> { redis ->
      redis.smembers(artifact.versionsKey)
        .map { objectMapper.readValue<DeliveryArtifactVersion>(it) }
        .sortedByDescending { it.version }
    }

  private fun JedisCommands.isRegistered(artifact: DeliveryArtifact) =
    sismember("keel.delivery_artifacts", artifact.asJson())

  private fun DeliveryArtifact.asJson() =
    objectMapper.writeValueAsString(this)

  private val DeliveryArtifact.versionsKey: String
    get() = "{keel.delivery_artifact_versions.%s.%s}".format(type, name)
}