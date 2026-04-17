-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.archivos (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  descarga_id uuid,
  nombre_archivo text NOT NULL UNIQUE,
  extension character varying,
  ruta_local text,
  ruta_supabase text,
  tamanio_bytes bigint,
  duracion_segundos integer,
  hash_md5 character varying,
  fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT archivos_pkey PRIMARY KEY (id),
  CONSTRAINT archivos_descarga_id_fkey FOREIGN KEY (descarga_id) REFERENCES public.descargas(id)
);
CREATE TABLE public.configuracion_sistema (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  clave character varying NOT NULL UNIQUE,
  valor text NOT NULL,
  tipo character varying,
  descripcion text,
  fecha_actualizacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT configuracion_sistema_pkey PRIMARY KEY (id)
);
CREATE TABLE public.descargas (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  url text NOT NULL,
  video_id text NOT NULL,
  titulo text,
  formato character varying,
  es_mp3 boolean DEFAULT false,
  quality character varying,
  tamanio_bytes bigint,
  estado character varying DEFAULT 'completado'::character varying CHECK (estado::text = ANY (ARRAY['completado'::character varying, 'error'::character varying, 'en_progreso'::character varying]::text[])),
  ip_cliente inet,
  user_agent text,
  fecha_descarga timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT descargas_pkey PRIMARY KEY (id)
);
CREATE TABLE public.errores_descargas (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  descarga_id uuid,
  url text NOT NULL,
  video_id text,
  mensaje_error text,
  tipo_error character varying,
  codigo_estado integer,
  ip_cliente inet,
  fecha_error timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT errores_descargas_pkey PRIMARY KEY (id),
  CONSTRAINT errores_descargas_descarga_id_fkey FOREIGN KEY (descarga_id) REFERENCES public.descargas(id)
);
CREATE TABLE public.estadisticas_diarias (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  fecha date NOT NULL UNIQUE,
  total_descargas integer DEFAULT 0,
  total_descargas_mp3 integer DEFAULT 0,
  total_descargas_video integer DEFAULT 0,
  total_bytes_descargados bigint DEFAULT 0,
  descargas_exitosas integer DEFAULT 0,
  descargas_fallidas integer DEFAULT 0,
  videos_unicos integer DEFAULT 0,
  ips_unicas integer DEFAULT 0,
  formato_mas_usado character varying,
  fecha_actualizacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT estadisticas_diarias_pkey PRIMARY KEY (id)
);
CREATE TABLE public.formatos_disponibles (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  video_id text NOT NULL,
  format_id character varying,
  extension character varying,
  resolucion integer,
  bitrate_audio integer,
  bitrate_video integer,
  tamanio_estimado bigint,
  disponible boolean DEFAULT true,
  CONSTRAINT formatos_disponibles_pkey PRIMARY KEY (id),
  CONSTRAINT formatos_disponibles_video_id_fkey FOREIGN KEY (video_id) REFERENCES public.videos_procesados(video_id),
  CONSTRAINT formatos_disponibles_video_id_fkey1 FOREIGN KEY (video_id) REFERENCES public.videos_procesados(video_id)
);
CREATE TABLE public.sesiones_activas (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  ip_cliente inet NOT NULL,
  fecha_conexion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  ultima_actividad timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  descargas_en_sesion integer DEFAULT 0,
  CONSTRAINT sesiones_activas_pkey PRIMARY KEY (id)
);
CREATE TABLE public.videos_procesados (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  video_id text NOT NULL UNIQUE,
  url text NOT NULL,
  titulo text,
  imagen_thumbnail text,
  duracion_segundos integer,
  autor text,
  fecha_obtencion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  veces_descargado integer DEFAULT 1,
  ultimo_acceso timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT videos_procesados_pkey PRIMARY KEY (id)
);
