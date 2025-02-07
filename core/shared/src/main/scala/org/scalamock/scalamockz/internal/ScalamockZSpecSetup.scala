package org.scalamock.scalamockz.internal

import org.scalamock.scalamockz.ScalamockZFactory
import zio.test.TestAspect.{after, before}
import zio.{Chunk, ZIO, ZLayer}
import zio.test.{testEnvironment, TestAspect, TestAspectAtLeastR, TestEnvironment, ZIOSpec}

private[scalamockz] trait ScalamockZSpecSetup extends ZIOSpec[TestEnvironment with ScalamockZFactory] {

  override def bootstrap: ZLayer[Any, Any, TestEnvironment with ScalamockZFactory] =
    // A placeholder layer to prevent compiler errors regarding a missing ScalamockZFactory.
    // This factory isn't actually used. The `live` implementation from aspects takes precedence.
    ZLayer.make[TestEnvironment with ScalamockZFactory](testEnvironment, ScalamockZFactory.dummy)

  // If the list of aspects here is updated, make sure to also update it in ScalamockZSpecSpec.aspects!
  override def aspects: Chunk[TestAspectAtLeastR[TestEnvironment with ScalamockZFactory]] = super.aspects :+
    before(ZIO.serviceWithZIO[ScalamockZFactory](_.initializeExpectations())) :+
    // We need to use orDie because the `aspects` method has the type
    // TestAspect[..., LowerE = Nothing, UpperE = Any],
    // while this expression without orDie has the type
    // TestAspect[..., LowerE = ExpectationException, UpperE = Nothing],
    // and `LowerE` is covariant.
    after(ZIO.serviceWithZIO[ScalamockZFactory](_.verifyExpectations().orDie)) :+
    TestAspect.fromLayer(ScalamockZFactory.live)
}
