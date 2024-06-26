package io.apicurio.registry.rules.compatibility.jsonschema.diff;

import io.apicurio.registry.rules.compatibility.jsonschema.JsonSchemaWrapperVisitor;
import io.apicurio.registry.rules.compatibility.jsonschema.wrapper.ReferenceSchemaWrapper;
import io.apicurio.registry.rules.compatibility.jsonschema.wrapper.SchemaWrapper;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;

import static io.apicurio.registry.rules.compatibility.jsonschema.diff.DiffType.REFERENCE_TYPE_TARGET_SCHEMA_ADDED;
import static io.apicurio.registry.rules.compatibility.jsonschema.diff.DiffType.REFERENCE_TYPE_TARGET_SCHEMA_REMOVED;
import static io.apicurio.registry.rules.compatibility.jsonschema.diff.DiffUtil.diffSubschemaAddedRemoved;

public class ReferenceSchemaDiffVisitor extends JsonSchemaWrapperVisitor {

    private DiffContext ctx;
    private final Schema referredOriginal;

    public ReferenceSchemaDiffVisitor(DiffContext ctx, Schema original) {
        this.ctx = ctx;
        if (original instanceof ReferenceSchema) {
            this.referredOriginal = ((ReferenceSchema) original).getReferredSchema();
        } else {
            this.referredOriginal = original;
        }
    }

    @Override
    public void visitReferenceSchema(ReferenceSchemaWrapper referenceSchema) {
        // TODO Can't use the schema itself, hashCode & equals would cause StackOverflowError, report a bug to
        // te library
        if (!ctx.visited.contains(referenceSchema.getLocation())) {
            ctx.visited.add(referenceSchema.getLocation());
            ctx = ctx.sub("[ref " + referenceSchema.getLocation() + "]");
            super.visitReferenceSchema(referenceSchema);
        } else {
            ctx.log("Reference recursion circuit breaker activated at: " + ctx.getPathUpdated());
        }
    }

    @Override
    public void visitReferredSchema(SchemaWrapper schema) {
        if (diffSubschemaAddedRemoved(ctx, referredOriginal, schema, REFERENCE_TYPE_TARGET_SCHEMA_ADDED,
                REFERENCE_TYPE_TARGET_SCHEMA_REMOVED)) {
            schema.accept(new SchemaDiffVisitor(ctx, referredOriginal));
        }
        super.visitReferredSchema(schema);
    }
}
