CREATE TABLE IF NOT EXISTS inquiries (
  id UUID PRIMARY KEY,
  tenant_id UUID NOT NULL,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(200) NOT NULL,
  company VARCHAR(150),
  message TEXT NOT NULL,
  category VARCHAR(40) NOT NULL DEFAULT 'unclassified',
  summary VARCHAR(500),
  status VARCHAR(30) NOT NULL DEFAULT 'new',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS ix_inquiries_tenant_status_created ON inquiries(tenant_id,status,created_at DESC);
