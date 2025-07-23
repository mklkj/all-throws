package org.jetbrains.kotlin.compiler.plugin.template.fir

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.impl.FirAnnotationArgumentMappingImpl
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

class AddThrowsAnnotationExtension(session: FirSession) : FirStatusTransformerExtension(session) {

    private val throwsFqName = FqName("kotlin.Throws")

    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
        return when (declaration) {
            is FirSimpleFunction, is FirConstructor -> {
                val member = declaration as FirMemberDeclaration
                when (member.status.visibility) {
                    Visibilities.Public -> true
                    Visibilities.Unknown -> true
                    else -> false
                }
            }

            else -> false
        }
    }

    override fun transformStatus(
        status: FirDeclarationStatus,
        function: FirSimpleFunction,
        containingClass: FirClassLikeSymbol<*>?,
        isLocal: Boolean,
    ): FirDeclarationStatus = transformStatus(status, function)

    override fun transformStatus(
        status: FirDeclarationStatus,
        constructor: FirConstructor,
        containingClass: FirClassLikeSymbol<*>?,
        isLocal: Boolean,
    ): FirDeclarationStatus = transformStatus(status, constructor)

    private fun transformStatus(
        status: FirDeclarationStatus,
        function: FirFunction,
    ): FirDeclarationStatus {
        val alreadyHasThrows = function.annotations.any {
            it.annotationTypeRef.coneType.classId?.asSingleFqName() == throwsFqName
        }

        if (!alreadyHasThrows) {
            val throwsAnnotation = createThrowsAnnotation()
            function.replaceAnnotations(function.annotations + throwsAnnotation)
        }

        return status
    }

    private fun createThrowsAnnotation(): FirAnnotation {
        val exceptionClassId = ClassId.topLevel(throwsFqName)

        return buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                coneType = ConeClassLikeTypeImpl(
                    lookupTag = ConeClassLikeLookupTagImpl(exceptionClassId),
                    typeArguments = emptyArray(),
                    isMarkedNullable = false
                )
            }
            argumentMapping = FirAnnotationArgumentMappingImpl(
                source = source,
                mapping = emptyMap(),
            )
        }
    }
}
